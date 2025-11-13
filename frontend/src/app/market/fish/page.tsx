'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Trade, ApiResponse, PageResponse, TradeStatus } from '@/type/trade';
import { fetchApi } from '@/lib/client';
import { useAuth } from '@/context/AuthContext';

export default function FishMarketPage() {
  const router = useRouter();
  const { isAuthenticated, loading: authLoading } = useAuth();
  const [posts, setPosts] = useState<Trade[]>([]);
  const [loading, setLoading] = useState(true);

  // 검색 입력 상태 (실제 적용 전)
  const [searchTerm, setSearchTerm] = useState('');
  const [searchType, setSearchType] = useState('전체'); // 전체, 제목, 본문, 태그
  const [priceRange, setPriceRange] = useState<[number, number]>([0, 100000]);

  // 실제 적용된 검색 조건 (API 호출용)
  const [appliedSearchTerm, setAppliedSearchTerm] = useState('');
  const [appliedPriceRange, setAppliedPriceRange] = useState<[number, number]>([0, 100000]);

  const [statusFilter, setStatusFilter] = useState<TradeStatus | '전체'>('전체');
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [sortType, setSortType] = useState('latest');
  const [pageInput, setPageInput] = useState('');
  const [pageGroupStart, setPageGroupStart] = useState(1);
  const [isInitialized, setIsInitialized] = useState(false);
  const postsPerPage = 8; // 4열 × 2줄
  const maxButtons = 5; // 한 번에 보여줄 버튼 개수

  // URL에서 returnToPage 파라미터 확인하여 페이지 및 검색 조건 복원
  useEffect(() => {
    if (typeof window !== 'undefined') {
      const searchParams = new URLSearchParams(window.location.search);
      const returnToPage = searchParams.get('returnToPage');
      const returnSearchTerm = searchParams.get('searchTerm');
      const returnMinPrice = searchParams.get('minPrice');
      const returnMaxPrice = searchParams.get('maxPrice');
      const returnStatus = searchParams.get('status');
      const returnSort = searchParams.get('sort');

      let hasRestoredState = false;

      if (returnToPage) {
        const pageNum = parseInt(returnToPage);
        if (!isNaN(pageNum) && pageNum > 0) {
          setCurrentPage(pageNum);
          // 페이지 그룹도 조정
          const newGroupStart = Math.floor((pageNum - 1) / maxButtons) * maxButtons + 1;
          setPageGroupStart(newGroupStart);
          hasRestoredState = true;
        }
      }

      // 검색 조건 복원
      if (returnSearchTerm) {
        setSearchTerm(returnSearchTerm);
        setAppliedSearchTerm(returnSearchTerm);
        hasRestoredState = true;
      }

      // 가격 범위 복원
      if (returnMinPrice || returnMaxPrice) {
        const minPrice = returnMinPrice ? parseInt(returnMinPrice) : 0;
        const maxPrice = returnMaxPrice ? parseInt(returnMaxPrice) : 100000;
        setPriceRange([minPrice, maxPrice]);
        setAppliedPriceRange([minPrice, maxPrice]);
        hasRestoredState = true;
      }

      // 상태 필터 복원
      if (returnStatus) {
        setStatusFilter(returnStatus as TradeStatus);
        hasRestoredState = true;
      }

      // 정렬 복원
      if (returnSort) {
        setSortType(returnSort);
        hasRestoredState = true;
      }

      // URL에서 파라미터 제거
      if (hasRestoredState) {
        window.history.replaceState({}, '', '/market/fish');
      }

      setIsInitialized(true);
    }
  }, []);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [authLoading, isAuthenticated, router]);

  useEffect(() => {
    if (isAuthenticated && isInitialized) {
      fetchPosts();
    }
  }, [isAuthenticated, isInitialized, currentPage, sortType, appliedSearchTerm, appliedPriceRange, statusFilter]);

  const fetchPosts = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: (currentPage - 1).toString(), // 0-based index
        size: postsPerPage.toString(),
        sort: sortType
      });

      // 검색어 추가
      if (appliedSearchTerm && appliedSearchTerm.trim()) {
        params.append('searchTerm', appliedSearchTerm.trim());
      }

      // 가격 범위 추가
      if (appliedPriceRange[0] > 0) {
        params.append('minPrice', appliedPriceRange[0].toString());
      }
      if (appliedPriceRange[1] < 100000) {
        params.append('maxPrice', appliedPriceRange[1].toString());
      }

      // 상태 필터 추가
      if (statusFilter !== '전체') {
        params.append('status', statusFilter);
      }

      const data: ApiResponse<PageResponse<Trade>> = await fetchApi(
        `/api/market/fish?${params.toString()}`
      );

      if (data.resultCode.startsWith('200')) {
        setPosts(data.data.content);
        setTotalPages(data.data.totalPages);
        setTotalElements(data.data.totalElements);
      }
    } catch (error) {
      // 인증 관련 에러는 조용히 처리 (로그인 페이지로 리다이렉트되므로)
      if (error instanceof Error && error.message.includes('Access Token')) {
        console.log('로그인이 필요합니다.');
      } else {
        console.error('Failed to load fish market posts:', error);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    // 입력 상태를 적용 상태로 복사
    setAppliedSearchTerm(searchTerm);
    setAppliedPriceRange(priceRange);
    // 검색 시 첫 페이지로 이동
    setCurrentPage(1);
    setPageGroupStart(1);
  };

  const handleStatusFilter = (status: TradeStatus | '전체') => {
    setStatusFilter(status);
    // 필터 변경 시 첫 페이지로 이동
    setCurrentPage(1);
    setPageGroupStart(1);
  };

  const paginate = (pageNumber: number) => setCurrentPage(pageNumber);

  const handlePageInputSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const page = parseInt(pageInput);
    if (!isNaN(page) && page >= 1 && page <= totalPages) {
      setCurrentPage(page);
      // 입력한 페이지에 맞게 그룹도 조정
      const newGroupStart = Math.floor((page - 1) / maxButtons) * maxButtons + 1;
      setPageGroupStart(newGroupStart);
      setPageInput('');
    } else {
      alert(`1부터 ${totalPages}까지의 페이지 번호를 입력해주세요.`);
    }
  };

  // 페이지 버튼 범위 계산 (최대 5개)
  const getPageNumbers = () => {
    const pages: number[] = [];
    const endPage = Math.min(pageGroupStart + maxButtons - 1, totalPages);

    for (let i = pageGroupStart; i <= endPage; i++) {
      pages.push(i);
    }

    return pages;
  };

  // 이전 버튼: 버튼 그룹을 이전으로 이동
  const handlePrevGroup = () => {
    const newStart = Math.max(1, pageGroupStart - maxButtons);
    setPageGroupStart(newStart);
  };

  // 다음 버튼: 버튼 그룹을 다음으로 이동
  const handleNextGroup = () => {
    const newStart = Math.min(pageGroupStart + maxButtons, totalPages - maxButtons + 1);
    if (newStart > pageGroupStart) {
      setPageGroupStart(newStart);
    }
  };

  // 이전 그룹이 있는지 확인
  const hasPrevGroup = pageGroupStart > 1;

  // 다음 그룹이 있는지 확인
  const hasNextGroup = pageGroupStart + maxButtons <= totalPages;

  // 인증 확인 중이거나 로그인되지 않은 경우
  if (authLoading || !isAuthenticated) {
    return <div className="p-8">Loading...</div>;
  }

  if (loading) {
    return <div className="p-8">Loading...</div>;
  }

  return (
    <div className="max-w-7xl mx-auto p-8">
      {/* 헤더 */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold">물고기 거래</h1>
      </div>

      {/* 검색 및 필터 */}
      <div className="mb-6 space-y-4">
        {/* 검색바 */}
        <div className="flex gap-3">
          <select
            value={searchType}
            onChange={(e) => setSearchType(e.target.value)}
            className="px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
          >
            <option value="전체">전체</option>
            <option value="제목">제목</option>
            <option value="본문">본문</option>
            <option value="태그">태그</option>
          </select>
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            className="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            onClick={handleSearch}
            className="px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 font-semibold transition"
          >
            검색
          </button>
        </div>

        {/* 가격 필터 & 상태 필터 */}
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-3">
            <span className="text-sm text-gray-600 font-medium">가격:</span>
            <input
              type="number"
              value={priceRange[0]}
              onChange={(e) => setPriceRange([Number(e.target.value), priceRange[1]])}
              className="w-32 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <span className="text-gray-600">~</span>
            <input
              type="number"
              value={priceRange[1]}
              onChange={(e) => setPriceRange([priceRange[0], Number(e.target.value)])}
              className="w-32 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <span className="text-gray-600">원</span>
          </div>

          <div className="flex items-center gap-3">
            <span className="text-sm text-gray-600 font-medium">상태:</span>
            <div className="flex gap-2">
              <button
                onClick={() => handleStatusFilter('전체')}
                className={`px-4 py-2 rounded-lg font-medium transition ${
                  statusFilter === '전체'
                    ? 'bg-blue-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                전체
              </button>
              <button
                onClick={() => handleStatusFilter(TradeStatus.SELLING)}
                className={`px-4 py-2 rounded-lg font-medium transition ${
                  statusFilter === TradeStatus.SELLING
                    ? 'bg-green-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                판매중
              </button>
              <button
                onClick={() => handleStatusFilter(TradeStatus.COMPLETED)}
                className={`px-4 py-2 rounded-lg font-medium transition ${
                  statusFilter === TradeStatus.COMPLETED
                    ? 'bg-gray-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                판매완료
              </button>
              <button
                onClick={() => handleStatusFilter(TradeStatus.CANCELLED)}
                className={`px-4 py-2 rounded-lg font-medium transition ${
                  statusFilter === TradeStatus.CANCELLED
                    ? 'bg-red-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                판매취소
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* 게시글 목록 */}
      <div>
        {/* 필터 결과 정보 */}
        <div className="mb-4 flex items-center justify-between">
          <p className="text-gray-600">
            전체 <span className="font-semibold text-blue-600">{totalElements}</span>개
          </p>
          <select
            value={sortType}
            onChange={(e) => {
              setSortType(e.target.value);
              setCurrentPage(1);
            }}
            className="px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="latest">최신순</option>
            <option value="price-asc">낮은 가격순</option>
            <option value="price-desc">높은 가격순</option>
          </select>
        </div>

        {/* 게시글 그리드 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            {posts.map((post) => {
              // 현재 검색 조건을 URL에 포함
              const detailParams = new URLSearchParams({ page: currentPage.toString() });
              if (appliedSearchTerm) detailParams.append('searchTerm', appliedSearchTerm);
              if (appliedPriceRange[0] > 0) detailParams.append('minPrice', appliedPriceRange[0].toString());
              if (appliedPriceRange[1] < 100000) detailParams.append('maxPrice', appliedPriceRange[1].toString());
              if (statusFilter !== '전체') detailParams.append('status', statusFilter);
              if (sortType !== 'latest') detailParams.append('sort', sortType);

              return (
              <Link
                key={post.tradeId}
                href={`/market/fish/${post.tradeId}?${detailParams.toString()}`}
                className="border rounded-lg overflow-hidden hover:shadow-xl transition-shadow bg-white"
              >
                <div className="h-48 bg-gray-200 relative">
                  {post.images && post.images.length > 0 ? (
                    <img
                      src={post.images[0]}
                      alt={post.title}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-gray-400">
                      이미지 없음
                    </div>
                  )}
                  {post.status !== TradeStatus.SELLING && (
                    <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center">
                      <span className="text-white font-bold text-lg">
                        {post.status === TradeStatus.COMPLETED ? '판매완료' : '판매취소'}
                      </span>
                    </div>
                  )}
                </div>

                <div className="p-4">
                  <div className="flex items-start justify-between mb-2">
                    <h3 className="font-semibold text-lg truncate flex-1">{post.title}</h3>
                    <span
                      className={`ml-2 px-2 py-1 text-xs rounded whitespace-nowrap ${
                        post.status === TradeStatus.SELLING
                          ? 'bg-green-100 text-green-800'
                          : post.status === TradeStatus.COMPLETED
                          ? 'bg-gray-100 text-gray-800'
                          : 'bg-red-100 text-red-800'
                      }`}
                    >
                      {post.status === TradeStatus.SELLING
                        ? '판매중'
                        : post.status === TradeStatus.COMPLETED
                        ? '판매완료'
                        : '판매취소'}
                    </span>
                  </div>
                  <p className="text-gray-600 text-sm line-clamp-2 mb-3">{post.description}</p>
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-xl font-bold text-blue-600">
                      {post.price.toLocaleString()}원
                    </span>
                    <span className="text-xs text-gray-500">{post.memberNickname}</span>
                  </div>
                  {post.category && (
                    <div className="mt-2">
                      <span className="inline-block px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded">
                        {post.category}
                      </span>
                    </div>
                  )}
                </div>
              </Link>
              );
            })}
        </div>

        {/* 게시글이 없을 때 */}
        {posts.length === 0 && (
          <div className="text-center py-16 text-gray-500">
            <p className="text-lg mb-2">게시글이 없습니다</p>
            <p className="text-sm">첫 게시글을 작성해보세요</p>
          </div>
        )}

        {/* 페이지네이션 */}
        <div className="flex justify-between items-center mt-8">
          <div className="flex-1"></div>
          {totalPages > 1 && (
            <div className="flex justify-center items-center gap-2 flex-1">
              <button
                onClick={handlePrevGroup}
                disabled={!hasPrevGroup}
                className="min-w-[60px] px-3 py-2 border rounded hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
              >
                이전
              </button>

              {getPageNumbers().map((number) => (
                <button
                  key={number}
                  onClick={() => paginate(number)}
                  className={`px-4 py-2 rounded ${
                    currentPage === number
                      ? 'bg-blue-500 text-white font-semibold'
                      : 'border hover:bg-gray-100'
                  }`}
                >
                  {number}
                </button>
              ))}

              <button
                onClick={handleNextGroup}
                disabled={!hasNextGroup}
                className="min-w-[60px] px-3 py-2 border rounded hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
              >
                다음
              </button>
            </div>
          )}
          <div className="flex-1 flex justify-end">
            <Link
              href="/market/fish/create"
              className="bg-blue-500 text-white px-6 py-2 rounded-lg hover:bg-blue-600 font-semibold transition"
            >
              글쓰기
            </Link>
          </div>
        </div>

        {/* 페이지 직접 입력 */}
        {totalPages > 1 && (
          <form onSubmit={handlePageInputSubmit} className="flex justify-center items-center gap-2 mt-4">
            <span className="text-sm text-gray-600">페이지 이동:</span>
            <input
              type="number"
              value={pageInput}
              onChange={(e) => setPageInput(e.target.value)}
              placeholder="번호"
              min="1"
              max={totalPages}
              className="w-20 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-center"
            />
            <button
              type="submit"
              className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 font-medium transition"
            >
              이동
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
