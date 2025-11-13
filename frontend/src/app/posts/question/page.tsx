"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { fetchApi } from "@/lib/client";
import { useAuth } from "@/context/AuthContext";

interface PostDto {
  id: number;
  title: string;
  nickname: string;
  createDate: string;
  commentCount: number;
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

export default function QuestionBoardPage() {
  const { isAuthenticated, loading: authLoading } = useAuth();
  const [posts, setPosts] = useState<PostDto[]>([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);

  const [keyword, setKeyword] = useState("");        // 🔍 검색어
  const [category, setCategory] = useState("ALL");   // 🏷 카테고리

  // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      window.location.href = '/login';
    }
  }, [isAuthenticated, authLoading]);

  const PAGE_SIZE = 10;

  // 🔹 게시글 로드
  const loadPosts = async (pageToLoad: number, searchKeyword = keyword, searchCategory = category) => {
    if (!isAuthenticated) return;
    
    setLoading(true);
    try {
      const query = new URLSearchParams({
        boardType: "QUESTION",
        page: String(pageToLoad - 1),
        size: String(PAGE_SIZE),
      });

      // 검색어
      if (searchKeyword.trim() !== "") {
        query.append("keyword", searchKeyword);
      }

      // 카테고리 추가
      if (searchCategory !== "ALL") {
        query.append("category", searchCategory);
      }

      const rsData: ApiResponse<PostListResponseDto> = await fetchApi(`/api/posts?${query.toString()}`);

      const data = rsData.data?.posts ?? [];
      setPosts(data);

      const totalCount = rsData.data?.totalCount ?? 0;
      setTotalPages(Math.ceil(totalCount / PAGE_SIZE));

      setPage(pageToLoad);
    } catch (err) {
      console.error(err);
      // 인증 오류인 경우 로그인 페이지로 리다이렉트
      if (err instanceof Error && (err.message.includes('CMN006') || err.message.includes('인증이 필요합니다'))) {
        window.location.href = '/login';
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPosts(1);
  }, []);

  // 🔹 페이지 버튼
  const renderPageButtons = () => {
    const buttons = [];
    for (let i = 1; i <= totalPages; i++) {
      buttons.push(
        <button
          key={i}
          onClick={() => loadPosts(i)}
          className={`px-3 py-1 border rounded ${i === page ? "bg-gray-800 text-white" : "bg-white"}`}
        >
          {i}
        </button>
      );
    }
    return buttons;
  };

  // 🔹 검색 제출
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    loadPosts(1, keyword, category);
  };

  // 로딩 중이거나 인증되지 않은 경우
  if (authLoading) {
    return (
      <div className="max-w-3xl mx-auto p-6">
        <p className="text-center">로딩 중...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="max-w-3xl mx-auto p-6">
        <p className="text-center">로그인이 필요합니다.</p>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto p-6">
      {/* 제목 + 게시 버튼 */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">질문 게시판</h1>
        <Link
          href="/posts/question/new"
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm font-medium"
        >
          새 글 작성
        </Link>
      </div>

      {/* 🔍 검색 영역 */}
      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        {/* 카테고리 선택 */}
        <select
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          className="border rounded px-2 py-1 text-sm"
        >
          <option value="ALL">전체 카테고리</option>
          <option value="FISH">물고기</option>
          <option value="AQUARIUM">수조</option>
          {/* 필요 시 다른 카테고리 추가 */}
        </select>

        {/* 검색어 입력 */}
        <input
          type="text"
          placeholder="검색어를 입력하세요"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          className="border rounded px-2 py-1 flex-grow text-sm"
        />

        <button type="submit" className="px-3 py-1 bg-gray-800 text-white rounded text-sm">
          검색
        </button>
      </form>

      {/* 게시글 리스트 */}
      <div className="border-t border-b divide-y">
        {posts.map((post) => (
          <Link
            key={post.id}
            href={`/posts/question/${post.id}`}
            className="block py-3 px-2 hover:bg-gray-50"
          >
            <p className="font-medium">{post.title}</p>
            <div className="flex justify-between text-sm text-gray-500 mt-1">
              <span>작성자: {post.nickname}</span>
              <span>{new Date(post.createDate).toLocaleDateString()}</span>
            </div>
          </Link>
        ))}
      </div>

      {loading && <p className="text-center py-4">로딩 중...</p>}

      <div className="flex justify-center gap-2 mt-6">
        {renderPageButtons()}
      </div>
    </div>
  );
}