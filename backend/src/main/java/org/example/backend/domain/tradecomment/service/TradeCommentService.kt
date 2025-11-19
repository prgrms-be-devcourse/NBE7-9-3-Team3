package org.example.backend.domain.tradecomment.service

import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.trade.entity.Trade
import org.example.backend.domain.trade.enums.BoardType
import org.example.backend.domain.trade.repository.TradeRepository
import org.example.backend.domain.tradecomment.dto.*
import org.example.backend.domain.tradecomment.entity.TradeComment
import org.example.backend.domain.tradecomment.repository.TradeCommentRepository
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TradeCommentService(
    private val tradeCommentRepository: TradeCommentRepository,
    private val memberRepository: MemberRepository,
    private val tradeRepository: TradeRepository
) {
    @Transactional
    fun createComment(
        boardType: BoardType,
        request: TradeCommentRequestDto
    ): TradeCommentResponseDto {
        val member = memberRepository.findById(request.memberId)
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }

        val trade = tradeRepository.findById(request.tradeId)
            .orElseThrow { BusinessException(ErrorCode.TRADE_NOT_FOUND) }

        validateBoardType(trade, boardType)

        val comment = request.toEntity(member, trade)
        val saved = tradeCommentRepository.save(comment)
        return TradeCommentResponseDto.from(saved)
    }

    fun getAllComment(boardType: BoardType, tradeId: Long): List<TradeCommentResponseDto> {
        val trade = tradeRepository.findById(tradeId)
            .orElseThrow { BusinessException(ErrorCode.TRADE_NOT_FOUND) }

        validateBoardType(trade, boardType)

        return tradeCommentRepository.findByTradeTradeId(tradeId)
            .map { TradeCommentResponseDto.from(it) }
    }

    @Transactional
    fun updateComment(updateRequest: TradeCommentUpdateRequestDto): TradeCommentResponseDto {
        val comment = tradeCommentRepository.findById(updateRequest.commentId)
            .orElseThrow { BusinessException(ErrorCode.TRADE_COMMENT_NOT_FOUND) }

        validateTrade(comment, updateRequest.tradeId)
        validateBoardType(comment.trade, updateRequest.boardType)
        validateCommentOwner(comment, updateRequest.memberId)

        comment.update(updateRequest.commentData.content)
        return TradeCommentResponseDto.from(comment)
    }

    @Transactional
    fun deleteComment(deleteRequest: TradeCommentDeleteRequestDto) {
        val comment = tradeCommentRepository.findById(deleteRequest.commentId)
            .orElseThrow { BusinessException(ErrorCode.TRADE_COMMENT_NOT_FOUND) }

        validateTrade(comment, deleteRequest.tradeId)
        validateBoardType(comment.trade, deleteRequest.boardType)
        validateCommentOwner(comment, deleteRequest.memberId)

        tradeCommentRepository.deleteById(deleteRequest.commentId)
    }

    private fun validateTrade(comment: TradeComment, tradeId: Long) {
        if (comment.trade.tradeId != tradeId) {
            throw BusinessException(ErrorCode.TRADE_COMMENT_POST_MISMATCH)
        }
    }

    private fun validateBoardType(trade: Trade, boardType: BoardType) {
        if (trade.boardType != boardType) {
            throw BusinessException(ErrorCode.TRADE_BOARD_TYPE_INVALID)
        }
    }

    private fun validateCommentOwner(comment: TradeComment, memberId: Long) {
        if (comment.member.memberId != memberId) {
            throw BusinessException(ErrorCode.TRADE_COMMENT_OWNER_MISMATCH)
        }
    }

    fun getMyTradeComments(memberId: Long): List<MyTradeCommentReadResponseDto> {
        val member = memberRepository.findById(memberId)
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }

        return tradeCommentRepository.findByMember(member)
            .sortedByDescending { it.createDate }
            .map { comment ->
                MyTradeCommentReadResponseDto(
                    id = comment.commentId,
                    tradeId = comment.trade.tradeId,
                    tradeTitle = comment.trade.title,
                    content = comment.content,
                    boardType = comment.trade.boardType.toString()
                )
            }
    }
}
