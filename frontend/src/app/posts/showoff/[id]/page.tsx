"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { fetchApi } from "@/lib/client";

interface PostReadResponseDto {
  id: number;
  title: string;
  content: string;
  nickname: string;
  createDate: string;
  images: string[];
  likeCount: number;
  liked?: boolean;
  following: boolean;
  authorId: number;
  isMine?: boolean; // 🔹 내 글 여부
}

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T;
}

interface CommentDto {
  id: number;
  author: string;
  content: string;
  isMine: boolean;
}

export default function PostDetailPage() {
  const params = useParams();
  const router = useRouter();
  const postId = params?.id;

  const [post, setPost] = useState<PostReadResponseDto | null>(null);
  const [comments, setComments] = useState<CommentDto[]>([]);
  const [currentImage, setCurrentImage] = useState(0);
  const [newComment, setNewComment] = useState("");
  const [following, setFollowing] = useState<boolean>(false);

  interface PostCommentReadResponseDto {
    id: number;
    content: string;
    nickname: string;
    isMine: boolean
  }

  const loadPostAndComments = async () => {
    if (!postId) return;
    try {
      // 게시글 불러오기
      const rsData: ApiResponse<PostReadResponseDto> = await fetchApi(`/api/posts/${postId}`);
      const postData = rsData.data ?? null;
      setPost(postData);
      if (postData) setFollowing(postData.following);

      // 댓글 불러오기
      const commentsRs: ApiResponse<PostCommentReadResponseDto[]> =
        await fetchApi(`/api/posts/comments?postId=${postId}`);

      setComments(
        (commentsRs.data ?? []).map((c) => ({
          id: c.id,
          author: c.nickname,
          content: c.content,
          isMine: c.isMine
        }))
      );
    } catch (err : any) {
      console.error(err);

      if (err.message.includes("비공개")) {
        alert("비공개 게시물입니다.");
        router.push("/posts/showoff"); // 목록 페이지로 리다이렉트
        return;
      }
    }
  };

  useEffect(() => {
    loadPostAndComments();
  }, [postId]);

  if (!post) return <p className="text-center py-10">게시글을 불러오는 중...</p>;

  const prevImage = () => {
    setCurrentImage((prev) => (prev === 0 ? post.images.length - 1 : prev - 1));
  };

  const nextImage = () => {
    setCurrentImage((prev) => (prev === post.images.length - 1 ? 0 : prev + 1));
  };

  // -------------------- 좋아요 토글 --------------------
  const handleToggleLike = async () => {
    if (!post) return;

    const newLiked = !post.liked;
    const newLikeCount = newLiked ? post.likeCount + 1 : post.likeCount - 1;

    // 1️⃣ 즉시 UI 반영
    setPost({ ...post, liked: newLiked, likeCount: newLikeCount });

    try {
      // 2️⃣ 서버 요청
      const rs: ApiResponse<{ liked: boolean; likeCount: number }> = await fetchApi(
        `/api/posts/${post.id}/likes`,
        { method: "POST" }
      );

      // 3️⃣ 서버 결과로 최종 동기화
      setPost({
        ...post,
        liked: rs.data?.liked ?? newLiked,
        likeCount: rs.data?.likeCount ?? newLikeCount,
      });
    } catch (err) {
      console.error(err);
      // 실패 시 롤백
      setPost(post);
    }
  };

  const handleToggleFollow = async () => {
    if (!post) return;
    const newFollowing = !following;
    setFollowing(newFollowing); // UI 즉시 반영

    try {
      if (newFollowing) {
        await fetchApi(`/api/follows/${post.authorId}`, { method: "POST" });
      } else {
        await fetchApi(`/api/follows/${post.authorId}`, { method: "DELETE" });
      }
      setPost({ ...post, following: newFollowing }); // post 상태에도 반영
    } catch (err) {
      console.error(err);
      setFollowing(!newFollowing); // 실패 시 롤백
      alert("팔로우 상태 변경 실패");
    }
  };

  const handleAddComment = async () => {
    if (!newComment.trim() || !postId) return;

    try {
      await fetchApi("/api/posts/comments", {
        method: "POST",
        body: JSON.stringify({ postId: Number(postId), content: newComment }),
      });

      setNewComment("");
      loadPostAndComments();
    } catch (err) {
      console.error(err);
      alert("댓글 작성 실패");
    }
  };

  const handleDeletePost = async () => {
    if (!confirm("정말 삭제하시겠습니까?")) return;
    try {
      await fetchApi(`/api/posts/${post.id}`, { method: "DELETE" });
      router.push("/posts/showoff");
    } catch (err) {
      console.error(err);
      alert("삭제 실패");
    }
  };

  return (
    <div className="max-w-3xl mx-auto p-6">
      {/* 제목 + 좋아요 + 팔로우 + 수정/삭제 */}
      <div className="flex justify-between items-start mb-4">
        <div>
          <h1 className="text-2xl font-bold">{post.title}</h1>

          <div className="flex gap-2 mt-2">
            <button
              onClick={handleToggleLike}
              className={`px-3 py-1 rounded ${post.liked ? "bg-red-500 text-white" : "bg-gray-200"}`}

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

        <div className="flex gap-2">
          <button
            onClick={() => {
              if (!post?.isMine) {
                alert("본인 글만 수정할 수 있습니다.");
                return;
              }
              router.push(`/posts/showoff/${post.id}/edit`);
            }}
            className="px-3 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            수정
          </button>
          <button
            onClick={async () => {
              if (!post?.isMine) {
                alert("본인 글만 삭제할 수 있습니다.");
                return;
              }
              if (!confirm("정말 삭제하시겠습니까?")) return;
              try {
                await fetchApi(`/api/posts/${post.id}`, { method: "DELETE" });
                router.push("/posts/showoff");
              } catch (err) {
                console.error(err);
                alert("삭제 실패");
              }
            }}
            className="px-3 py-1 text-sm bg-red-500 text-white rounded hover:bg-red-600"
          >
            삭제
          </button>
        </div>
      </div>

      {/* 작성자 + 작성일 */}
      <div className="flex justify-between text-sm text-gray-500 mb-4">
        <span>작성자: {post.nickname}</span>
        <span>{new Date(post.createDate).toLocaleDateString()}</span>
      </div>

      {/* 이미지 슬라이더 */}
      {post.images.length > 0 && (
        <div className="relative mb-6">
          <img
            src={post.images[currentImage]}
            alt={`image-${currentImage}`}
            className="w-full h-96 object-cover rounded"
          />
          {post.images.length > 1 && (
            <>
              <button
                onClick={prevImage}
                className="absolute top-1/2 left-2 transform -translate-y-1/2 bg-black bg-opacity-30 text-white px-2 py-1 rounded"
              >
                ◀
              </button>
              <button
                onClick={nextImage}
                className="absolute top-1/2 right-2 transform -translate-y-1/2 bg-black bg-opacity-30 text-white px-2 py-1 rounded"
              >
                ▶
              </button>
            </>
          )}
        </div>
      )}

      {/* 게시글 내용 */}
      <p className="text-gray-700 mb-6 whitespace-pre-wrap">{post.content}</p>

      {/* 댓글 영역 */}
      <div className="border-t border-gray-300 pt-4">
        {comments.map((comment) => (
          <div key={comment.id} className="mb-2 flex justify-between items-center">
            <div>
              <span className="font-semibold">{comment.author}:</span>{" "}
              <span>{comment.content}</span>
            </div>
            <button
              onClick={async () => {
                if (!comment.isMine) {
                  alert("본인 댓글만 삭제할 수 있습니다.");
                  return;
                }

                if (!confirm("댓글을 삭제하시겠습니까?")) return;

                try {
                  await fetchApi(`/api/posts/comments/${comment.id}`, { method: "DELETE" });
                  loadPostAndComments();
                } catch (err) {
                  console.error(err);
                  alert("댓글 삭제 실패");
                }
              }}
              className="ml-2 text-red-500 hover:underline text-sm"
            >
              삭제
            </button>
          </div>
        ))}

        {/* 댓글 입력 */}
        <div className="flex mt-4 gap-2">
          <input
            type="text"
            placeholder="댓글쓰기"
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            className="flex-1 border border-gray-300 rounded px-3 py-1"
          />
          <button
            onClick={handleAddComment}
            className="bg-gray-300 hover:bg-gray-400 px-4 py-1 rounded"
          >
            등록
          </button>
        </div>
      </div>
    </div>
  );
}
