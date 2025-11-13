package org.example.backend.domain.trade.service;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.trade.dto.PageResponseDto;
import org.example.backend.domain.trade.dto.TradeCreateRequestDto;
import org.example.backend.domain.trade.dto.TradeRequestDto;
import org.example.backend.domain.trade.dto.TradeResponseDto;
import org.example.backend.domain.trade.dto.TradeSearchRequestDto;
import org.example.backend.domain.trade.dto.TradeUpdateRequestDto;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.enums.BoardType;
import org.example.backend.domain.trade.repository.TradeRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.image.ImageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TradeService {

    private final TradeRepository tradeRepository;
    private final MemberRepository memberRepository;
    private final ImageService imageService;

    @Transactional
    public TradeResponseDto createTrade(TradeCreateRequestDto request) {
        Member member = memberRepository.findById(request.memberId())
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Trade trade = request.toEntity(member);

        if (request.imageUrls() != null) {
            request.imageUrls().forEach(trade::addImage);
        }

        return TradeResponseDto.from(tradeRepository.save(trade));
    }

    public PageResponseDto<TradeResponseDto> getAllTrade(BoardType boardType,
        TradeSearchRequestDto searchRequest) {

        if (boardType == null) {
            throw new BusinessException(ErrorCode.TRADE_BOARD_TYPE_INVALID);
        }

        Sort sort = switch (searchRequest.sort()) {
            case "price-asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price-desc" -> Sort.by(Sort.Direction.DESC, "price");
            default -> Sort.by(Sort.Direction.DESC, "createDate");
        };

        Pageable pageable = PageRequest.of(searchRequest.page(), searchRequest.size(), sort);

        Page<Trade> tradePage;
        if (searchRequest.hasSearchTerm() || searchRequest.hasPriceFilter()
            || searchRequest.hasStatusFilter()) {
            tradePage = tradeRepository.searchTrades(
                boardType,
                searchRequest.searchTerm(),
                searchRequest.minPrice() != null ? searchRequest.minPrice().longValue() : null,
                searchRequest.maxPrice() != null ? searchRequest.maxPrice().longValue() : null,
                searchRequest.status(),
                pageable
            );
        } else {
            tradePage = tradeRepository.findByBoardType(boardType, pageable);
        }

        Page<TradeResponseDto> responsePage = tradePage.map(TradeResponseDto::from);
        return PageResponseDto.from(responsePage);
    }

    public TradeResponseDto getTrade(BoardType boardType, Long id) {
        Trade trade = findTradeById(id);
        validateBoardType(trade, boardType);

        return TradeResponseDto.from(trade);
    }

    @Transactional
    public TradeResponseDto updateTrade(TradeUpdateRequestDto updateRequest) {
        Trade trade = findTradeById(updateRequest.tradeId());
        validateBoardType(trade, updateRequest.boardType());
        validateTradeOwner(trade, updateRequest.memberId());

        TradeRequestDto request = updateRequest.tradeData();
        trade.update(
            request.title(),
            request.description(),
            request.price(),
            request.status(),
            request.category()
        );

        updateTradeImages(trade, updateRequest.imageUrls());

        return TradeResponseDto.from(trade);
    }

    @Transactional
    public void deleteTrade(BoardType boardType, Long tradeId, Long memberId) {
        Trade trade = findTradeById(tradeId);
        validateBoardType(trade, boardType);
        validateTradeOwner(trade, memberId);

        // S3 이미지 삭제
        List<String> imageUrls = trade.getImageUrls();
        if (!imageUrls.isEmpty()) {
            imageService.deleteFiles(imageUrls);
        }

        // Trade 엔티티 삭제 (cascade로 TradeImage도 함께 삭제됨)
        tradeRepository.deleteById(tradeId);
    }

    public PageResponseDto<TradeResponseDto> getMyTrades(Long memberId, BoardType boardType,
        int page, int size) {

        Sort sort = Sort.by(Sort.Direction.DESC, "createDate");

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Trade> tradePage = tradeRepository.findMyTrades(memberId, boardType, pageable);

        Page<TradeResponseDto> responsePage = tradePage.map(TradeResponseDto::from);
        return PageResponseDto.from(responsePage);
    }

    private void validateBoardType(Trade trade, BoardType boardType) {
        if (!trade.getBoardType().equals(boardType)) {
            throw new BusinessException(ErrorCode.TRADE_BOARD_TYPE_INVALID);
        }
    }

    private void validateTradeOwner(Trade trade, Long memberId) {
        if (!trade.getMember().getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.TRADE_OWNER_MISMATCH);
        }
    }

    public Trade findTradeById(Long tradeId) {
        return tradeRepository.findById(tradeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));
    }

    private void updateTradeImages(Trade trade, List<String> newImageUrls) {
        if (newImageUrls == null) {
            return;
        }

        List<String> oldImageUrls = trade.getImageUrls();

        // 삭제할 이미지: 기존에는 있었는데 새 목록에는 없는 것
        List<String> toDelete = oldImageUrls.stream()
            .filter(url -> !newImageUrls.contains(url))
            .toList();

        // S3에서 삭제
        if (!toDelete.isEmpty()) {
            imageService.deleteFiles(toDelete);
        }

        // DB 이미지 목록 갱신
        trade.clearImages();
        newImageUrls.forEach(trade::addImage);
    }
}
