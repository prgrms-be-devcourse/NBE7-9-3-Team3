"use client";

import { useEffect, useState, useRef, useCallback } from "react";
import Link from "next/link";
import { fetchApi } from "@/lib/client";
import { useAuth } from "@/context/AuthContext";

interface PostDto {
  id: number;
  title: string;
  content: string;
  nickname: string;
  createDate: string;
  images: string[];
  likeCount: number;
  liked?: boolean; // 로그인 사용자 좋아요 여부
  following: boolean;
  authorId: number;
  isMine?: boolean;
}

interface PostListResponseDto {
  posts: PostDto[];
  totalCount: number;
}

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T;
}

interface FollowUser {
  memberId: number;
  nickname: string;
  profileImage: string | null;
  following?: boolean;
}

// -------------------- PostItem --------------------
function PostItem({
  post,
  onLike,
  onFollowChange,
}: {
  post: PostDto;
  onLike: (liked: boolean, likeCount: number) => void;
  onFollowChange: (following: boolean) => void;
}) {
  const [currentImage, setCurrentImage] = useState(0);

  const nextImage = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setCurrentImage((prev) => (prev + 1) % post.images.length);
  };

  const prevImage = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setCurrentImage((prev) => (prev - 1 + post.images.length) % post.images.length);
  };

  const handleToggleLike = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    const newLiked = !post.liked;
    const newLikeCount = newLiked ? post.likeCount + 1 : post.likeCount - 1;

    onLike(newLiked, newLikeCount);

    try {
      const rs: ApiResponse<{ liked: boolean; likeCount: number }> = await fetchApi(
        `/api/posts/${post.id}/likes`,
        { method: "POST" }
      );
      onLike(rs.data?.liked ?? newLiked, rs.data?.likeCount ?? newLikeCount);    } catch (err) {
      console.error(err);
      onLike(post.liked ?? false, post.likeCount);
    }
  };

  const handleToggleFollow = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    const newFollowing = !post.following;

    try {
      if (newFollowing) {
        await fetchApi(`/api/follows/${post.authorId}`, { method: "POST" });
      } else {
        await fetchApi(`/api/follows/${post.authorId}`, { method: "DELETE" });
      }
      onFollowChange(newFollowing); // 부모 상태를 업데이트
    } catch (err) {
      console.error(err);
      alert("팔로우 상태 변경 실패");
    }
  };

  return (
    <div className="border-b border-gray-200 pb-4 mb-4 last:mb-0 last:pb-0 last:border-0 hover:bg-gray-50 p-2 rounded">
      <Link href={`/posts/showoff/${post.id}`} className="block">
        <p className="font-bold">{post.title}</p>
        {post.images.length > 0 && (
          <div className="relative w-full h-64 my-2 bg-gray-100 rounded overflow-hidden flex items-center justify-center">
            <img
              src={post.images[currentImage]}
              alt={post.title}
              className="max-w-full max-h-full object-contain"
            />
            {post.images.length > 1 && (
              <>
                <button
                  className="absolute top-1/2 left-2 transform -translate-y-1/2 bg-black bg-opacity-40 text-white p-1 rounded-full"
                  onClick={prevImage}
                >
                  ◀
                </button>
                <button
                  className="absolute top-1/2 right-2 transform -translate-y-1/2 bg-black bg-opacity-40 text-white p-1 rounded-full"
                  onClick={nextImage}
                >
                  ▶
                </button>
              </>
            )}
          </div>
        )}

        <p className="text-gray-600">{post.content}</p>
        <div className="flex justify-between text-sm text-gray-500 mt-1">
          <span>작성자: {post.nickname}</span>
          <span>{new Date(post.createDate).toLocaleDateString()}</span>
        </div>
      </Link>

      <div className="flex gap-2 mt-2">
        <button
          onClick={handleToggleLike}
          className={`px-2 py-1 rounded ${post.liked ? "bg-red-500 text-white" : "bg-gray-200"}`}
        >
          ❤️ {post.likeCount}
        </button>

        <button
          onClick={handleToggleFollow}
          className={`px-2 py-1 rounded ${post.isMine
              ? "bg-green-500 text-white"         // 🔹 내 글이면 초록
              : post.following
                ? "bg-gray-400 text-white"         // 팔로잉
                : "bg-blue-500 text-white"         // 팔로우
            }`}
          disabled={post.isMine} // 내 글이면 클릭 불가
        >
          {post.isMine ? "내 글" : post.following ? "팔로잉" : "팔로우"}
        </button>
      </div>
    </div>
  );
}

// -------------------- PostListPage --------------------
export default function PostListPage() {
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const [activeTab, setActiveTab] = useState<"ALL" | "FOLLOWING">("ALL");
  const [posts, setPosts] = useState<PostDto[]>([]);
  const [totalCount, setTotalCount] = useState(0);

  // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      window.location.href = '/login';
    }
  }, [isAuthenticated, authLoading]);
  
  // 회원 검색 관련 상태
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<FollowUser[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [showSearchDropdown, setShowSearchDropdown] = useState(false);
  const [showSearchUI, setShowSearchUI] = useState(false);
  const [unfollowing, setUnfollowing] = useState<Set<number>>(new Set());
  const [lastSearchQuery, setLastSearchQuery] = useState(''); // 마지막 검색어 저장
  const [followingList, setFollowingList] = useState<FollowUser[]>([]); // 팔로잉 목록

  const pageRef = useRef(0);
  const hasNextRef = useRef(true);
  const loadingRef = useRef(false);

  const PAGE_SIZE = 10;

  const loadPosts = useCallback(
    async (reset = false) => {
      if (!isAuthenticated || loadingRef.current || (!hasNextRef.current && !reset)) return;
      loadingRef.current = true;

      const pageToLoad = reset ? 0 : pageRef.current;

      try {
        const rsData: ApiResponse<PostListResponseDto> = await fetchApi(
          `/api/posts?boardType=SHOWOFF&filterType=${activeTab}&page=${pageToLoad}&size=${PAGE_SIZE}`
        );

        const data = rsData.data?.posts ?? [];
        const total = rsData.data?.totalCount ?? 0;

        setPosts((prev) => (reset ? data : [...prev, ...data]));
        setTotalCount(total);

        pageRef.current = pageToLoad + 1;
        hasNextRef.current = (reset ? data.length : pageRef.current * PAGE_SIZE) < total;
      } catch (err) {
        console.error(err);
        // 인증 오류인 경우 로그인 페이지로 리다이렉트
        if (err instanceof Error && (err.message.includes('CMN006') || err.message.includes('인증이 필요합니다'))) {
          window.location.href = '/login';
        }
      } finally {
        loadingRef.current = false;
      }
    },
    [activeTab, isAuthenticated]
  );

  useEffect(() => {
    pageRef.current = 0;
    hasNextRef.current = true;
    loadPosts(true);
  }, [activeTab, loadPosts]);

  const handleScroll = useCallback(() => {
    if (
      window.innerHeight + window.scrollY >= document.body.offsetHeight - 300 &&
      hasNextRef.current
    ) {
      loadPosts();
    }
  }, [loadPosts]);

  useEffect(() => {
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [handleScroll]);

  // 드롭다운 외부 클릭 시 닫기
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement;
      if (!target.closest('.search-dropdown-container')) {
        setShowSearchDropdown(false);
      }
    };

    if (showSearchDropdown) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [showSearchDropdown]);

  // 컴포넌트 언마운트 시 타이머 정리
  useEffect(() => {
    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    };
  }, []);

  // -------------------- 좋아요 상태 업데이트 함수 --------------------
  const handleLikeUpdate = (postId: number, liked: boolean, likeCount: number) => {
    setPosts((prev) =>
      prev.map((p) => (p.id === postId ? { ...p, liked, likeCount } : p))
    );
  };

  const handleFollowUpdate = (authorId: number, following: boolean) => {
    setPosts((prev) =>
      prev.map((p) => (p.authorId === authorId ? { ...p, following } : p))
    );
  };

  // 팔로잉 목록 가져오기
  const fetchFollowingList = async () => {
    if (!user) return;
    
    try {
      const response: ApiResponse<{users: FollowUser[], totalCount: number}> = await fetchApi(
        `/api/follows/${user.memberId}/followings`
      );
      if (response.data) {
        setFollowingList(response.data.users);
      }
    } catch (error) {
      console.error('팔로잉 목록 조회 실패:', error);
    }
  };

  // 회원 검색 UI 토글
  const toggleSearchUI = () => {
    setShowSearchUI(!showSearchUI);
    if (showSearchUI) {
      // 검색 UI를 닫을 때 검색 상태 초기화
      setSearchQuery('');
      setSearchResults([]);
      setShowSearchDropdown(false);
      setLastSearchQuery('');
      // 타이머도 취소
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    }
  };

  // Debouncing을 위한 타이머 ref
  const searchTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // 회원 검색 함수 (debounced)
  const handleSearch = async (query: string) => {
    if (!query.trim()) {
      setSearchResults([]);
      setIsSearching(false);
      setShowSearchDropdown(false);
      setLastSearchQuery('');
      return;
    }

    try {
      setIsSearching(true);
      const response: ApiResponse<{members: FollowUser[], totalCount: number}> = await fetchApi(
        `/api/members/search?nickname=${encodeURIComponent(query)}&page=0&size=20`
      );
      if (response.data) {
        setSearchResults(response.data.members);
        setShowSearchDropdown(true);
        setLastSearchQuery(query); // 검색 성공 시 마지막 검색어 저장
      }
    } catch (error) {
      console.error('검색 실패:', error);
    } finally {
      setIsSearching(false);
    }
  };

  // Debounced 검색 함수
  const debouncedSearch = (query: string) => {
    // 이전 타이머가 있으면 취소
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }
    
    // 검색 결과가 없고, 현재 검색어가 마지막 검색어를 포함하는 경우 검색하지 않음
    if (searchResults.length === 0 && lastSearchQuery && query.includes(lastSearchQuery)) {
      return;
    }
    
    // 500ms 후에 검색 실행
    searchTimeoutRef.current = setTimeout(() => {
      handleSearch(query);
    }, 500);
  };

  // 팔로우/언팔로우 함수
  const handleFollow = async (memberId: number) => {
    try {
      setUnfollowing(prev => new Set(prev).add(memberId));
      
      const response = await fetchApi(`/api/follows/${memberId}`, {
        method: 'POST',
      });
      
      if (response.resultCode === '200') {
        // 검색 결과에서 팔로우 상태만 업데이트 (제거하지 않음)
        setSearchResults(prev => prev.map(user => 
          user.memberId === memberId ? { ...user, following: true } : user
        ));

        setPosts(prev => prev.map(post => 
          post.authorId === memberId ? { ...post, following: true } : post
        ));
        
        // 팔로잉 탭이 활성화되어 있으면 글 목록 새로고침
        if (activeTab === 'FOLLOWING') {
          loadPosts(true);
        }
      } else {
        console.error('팔로우 실패:', response.msg);
      }
    } catch (error) {
      console.error('팔로우 실패:', error);
    } finally {
      setUnfollowing(prev => {
        const newSet = new Set(prev);
        newSet.delete(memberId);
        return newSet;
      });
    }
  };

  const handleUnfollow = async (memberId: number) => {
    try {
      setUnfollowing(prev => new Set(prev).add(memberId));
      
      const response = await fetchApi(`/api/follows/${memberId}`, {
        method: 'DELETE',
      });
      
      if (response.resultCode === '200') {
        // 검색 결과에서 팔로우 상태만 업데이트 (제거하지 않음)
        setSearchResults(prev => prev.map(user => 
          user.memberId === memberId ? { ...user, following: false } : user
        ));

        setPosts(prev => prev.map(post => 
          post.authorId === memberId ? { ...post, following: false } : post
        ));
        
        // 팔로잉 탭이 활성화되어 있으면 글 목록 새로고침
        if (activeTab === 'FOLLOWING') {
          loadPosts(true);
        }
      } else {
        console.error('언팔로우 실패:', response.msg);
      }
    } catch (error) {
      console.error('언팔로우 실패:', error);
    } finally {
      setUnfollowing(prev => {
        const newSet = new Set(prev);
        newSet.delete(memberId);
        return newSet;
      });
    }
  };

  // 로딩 중이거나 인증되지 않은 경우
  if (authLoading) {
    return (
      <div className="max-w-2xl mx-auto p-6">
        <p className="text-center">로딩 중...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="max-w-2xl mx-auto p-6">
        <p className="text-center">로그인이 필요합니다.</p>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto p-6">
      <div className="flex items-center mb-6 justify-between">
        <div className="flex gap-4">
          {["ALL", "FOLLOWING"].map((tab) => (
            <button
              key={tab}
              className={`px-4 py-2 rounded ${activeTab === tab ? "bg-gray-800 text-white" : "bg-gray-100"
                }`}
              onClick={() => setActiveTab(tab as "ALL" | "FOLLOWING")}
            >
              {tab === "ALL" ? "전체" : "팔로잉"}
            </button>
          ))}
        </div>

        <div className="flex gap-2">
          {isAuthenticated && (
            <button
              onClick={toggleSearchUI}
              className={`px-4 py-2 rounded text-sm font-medium transition-colors ${
                showSearchUI 
                  ? 'bg-gray-600 text-white' 
                  : 'bg-gray-500 text-white hover:bg-gray-600'
              }`}
            >
              {showSearchUI ? '검색 닫기' : '회원 검색'}
            </button>
          )}
          <Link
            href="/posts/showoff/new"
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm font-medium"
          >
            새 글 작성
          </Link>
        </div>
      </div>

      {/* 회원 검색 섹션 */}
      {isAuthenticated && showSearchUI && (
        <div className="mb-6 relative search-dropdown-container">
          <div className="flex space-x-4">
            <div className="flex-1 relative">
              <input
                type="text"
                placeholder="닉네임을 입력하여 회원을 찾아보세요..."
                value={searchQuery}
                onChange={(e) => {
                  setSearchQuery(e.target.value);
                  if (e.target.value.trim()) {
                    debouncedSearch(e.target.value);
                  } else {
                    setShowSearchDropdown(false);
                    setSearchResults([]);
                    // 타이머도 취소
                    if (searchTimeoutRef.current) {
                      clearTimeout(searchTimeoutRef.current);
                    }
                  }
                }}
                onKeyPress={(e) => {
                  if (e.key === 'Enter') {
                    handleSearch(searchQuery);
                  }
                }}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              {isSearching && (
                <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-500"></div>
                </div>
              )}
            </div>
            <button
              onClick={() => handleSearch(searchQuery)}
              disabled={isSearching}
              className="px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed"
            >
              {isSearching ? '검색 중...' : '검색'}
            </button>
          </div>

          {/* 검색 결과 드롭다운 */}
          {showSearchDropdown && searchResults.length > 0 && (
            <div className="absolute top-full left-0 right-0 mt-2 bg-white border border-gray-200 rounded-lg shadow-lg z-50 max-h-80 overflow-y-auto">
              <div className="p-2">
                <div className="text-sm text-gray-500 mb-2 px-2">검색 결과</div>
                {searchResults.map((user) => (
                  <div key={`search-${user.memberId}`} className="p-3 hover:bg-gray-50 transition-colors border-b border-gray-100 last:border-b-0">
                    <div className="flex items-center space-x-3">
                      {/* 프로필 이미지 */}
                      <div className="flex-shrink-0">
                        {user.profileImage ? (
                          <img
                            src={user.profileImage}
                            alt={user.nickname}
                            className="h-10 w-10 rounded-full object-cover border-2 border-gray-200"
                          />
                        ) : (
                          <div className="h-10 w-10 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center text-white text-sm font-bold">
                            {user.nickname.charAt(0).toUpperCase()}
                          </div>
                        )}
                      </div>
                      
                      {/* 사용자 정보 */}
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-gray-900 truncate">
                          {user.nickname}
                        </p>
                        <p className="text-xs text-gray-500">
                          ID: {user.memberId}
                        </p>
                      </div>
                      
                      {/* 액션 버튼 */}
                      <div className="flex-shrink-0">
                        {user.following ? (
                          <button
                            onClick={() => handleUnfollow(user.memberId)}
                            disabled={unfollowing.has(user.memberId)}
                            className={`px-3 py-1 text-xs rounded transition-colors ${
                              unfollowing.has(user.memberId)
                                ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                                : 'bg-red-500 text-white hover:bg-red-600'
                            }`}
                          >
                            {unfollowing.has(user.memberId) ? '언팔로우 중...' : '언팔로우'}
                          </button>
                        ) : (
                          <button
                            onClick={() => handleFollow(user.memberId)}
                            disabled={unfollowing.has(user.memberId)}
                            className={`px-3 py-1 text-xs rounded transition-colors ${
                              unfollowing.has(user.memberId)
                                ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                                : 'bg-blue-500 text-white hover:bg-blue-600'
                            }`}
                          >
                            {unfollowing.has(user.memberId) ? '팔로우 중...' : '팔로우'}
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* 검색 결과가 없을 때 */}
          {showSearchDropdown && searchResults.length === 0 && searchQuery.trim() && !isSearching && (
            <div className="absolute top-full left-0 right-0 mt-2 bg-white border border-gray-200 rounded-lg shadow-lg z-50">
              <div className="p-4 text-center">
                <svg className="mx-auto h-8 w-8 text-gray-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                <p className="text-sm text-gray-500">검색 결과가 없습니다</p>
              </div>
            </div>
          )}
        </div>
      )}

      {posts.map((post) => (
        <PostItem
          key={post.id}
          post={post}
          onLike={(liked, likeCount) => handleLikeUpdate(post.id, liked, likeCount)}
          onFollowChange={(following) => handleFollowUpdate(post.authorId, following)}
        />
      ))}

      {loadingRef.current && <p className="text-center py-4">Loading...</p>}
      {!hasNextRef.current && posts.length > 0 && (
        <p className="text-center py-4 text-gray-500">더 이상 게시물이 없습니다.</p>
      )}
    </div>
  );
}
