package org.example.backend.domain.tradecomment.service;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.repository.TradeRepository;
import org.example.backend.domain.tradecomment.dto.MyTradeCommentReadResponseDto;
import org.example.backend.domain.tradecomment.dto.TradeCommentDeleteRequestDto;
import org.example.backend.domain.tradecomment.dto.TradeCommentRequestDto;
import org.example.backend.domain.tradecomment.dto.TradeCommentResponseDto;
import org.example.backend.domain.tradecomment.dto.TradeCommentUpdateRequestDto;
import org.example.backend.domain.tradecomment.entity.TradeComment;
import org.example.backend.domain.tradecomment.repository.TradeCommentRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TradeCommentService {

    private final TradeCommentRepository tradeCommentRepository;
    private final MemberRepository memberRepository;
    private final TradeRepository tradeRepository;

    @Transactional
    public TradeCommentResponseDto createComment(BoardType boardType,
        TradeCommentRequestDto request) {
        Member member = memberRepository.findById(request.memberId())
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Trade trade = tradeRepository.findById(request.tradeId())
            .orElseThrow(
                () -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        validateBoardType(trade, boardType);

        TradeComment comment = request.toEntity(member, trade);
        TradeComment saved = tradeCommentRepository.save(comment);
        return TradeCommentResponseDto.from(saved);
    }

    public List<TradeCommentResponseDto> getAllComment(BoardType boardType, Long tradeId) {
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(
                () -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        validateBoardType(trade, boardType);

        return tradeCommentRepository.findByTradeTradeId(tradeId).stream()
            .map(TradeCommentResponseDto::from)
            .toList();
    }

    @Transactional
    public TradeCommentResponseDto updateComment(TradeCommentUpdateRequestDto updateRequest) {
        TradeComment comment = tradeCommentRepository.findById(updateRequest.commentId())
            .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_COMMENT_NOT_FOUND));

        validateTrade(comment, updateRequest.tradeId());
        validateBoardType(comment.getTrade(), updateRequest.boardType());
        validateCommentOwner(comment, updateRequest.memberId());

        comment.update(updateRequest.commentData().content());
        return TradeCommentResponseDto.from(comment);
    }

    @Transactional
    public void deleteComment(TradeCommentDeleteRequestDto deleteRequest) {
        TradeComment comment = tradeCommentRepository.findById(deleteRequest.commentId())
            .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_COMMENT_NOT_FOUND));

        validateTrade(comment, deleteRequest.tradeId());
        validateBoardType(comment.getTrade(), deleteRequest.boardType());
        validateCommentOwner(comment, deleteRequest.memberId());

        tradeCommentRepository.deleteById(deleteRequest.commentId());
    }

    private void validateTrade(TradeComment comment, Long tradeId) {
        if (!comment.getTrade().getTradeId().equals(tradeId)) {
            throw new BusinessException(ErrorCode.TRADE_COMMENT_POST_MISMATCH);
        }
    }

    private void validateBoardType(Trade trade, BoardType boardType) {
        if (!trade.getBoardType().equals(boardType)) {
            throw new BusinessException(ErrorCode.TRADE_BOARD_TYPE_INVALID);
        }
    }

    private void validateCommentOwner(TradeComment comment, Long memberId) {
        if (!comment.getMember().getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.TRADE_COMMENT_OWNER_MISMATCH);
        }
    }

    public List<MyTradeCommentReadResponseDto> getMyTradeComments(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return tradeCommentRepository.findByMember(member).stream()
            .sorted(Comparator.comparing(TradeComment::getCreateDate).reversed())
            .map(comment -> new MyTradeCommentReadResponseDto(
                comment.getCommentId(),
                comment.getTrade().getTradeId(),
                comment.getTrade().getTitle(),
                comment.getContent(),
                comment.getTrade().getBoardType().toString()
            ))
            .toList();
    }
}
