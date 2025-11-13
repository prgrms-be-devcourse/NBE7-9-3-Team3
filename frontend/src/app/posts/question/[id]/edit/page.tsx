'use client';

import { useState, useEffect } from 'react';
import { fetchApi } from '@/lib/client';
import { useRouter, useParams } from 'next/navigation';
import { uploadImages } from '@/lib/uploadImage';

interface PostDto {
  id: number;
  title: string;
  content: string;
  images: string[]; // 서버에 저장된 기존 이미지 URL
}

export default function PostEditPage() {
  const router = useRouter();
  const { id } = useParams<{ id: string }>();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [existingImages, setExistingImages] = useState<string[]>([]); // 서버에 저장된 기존 이미지
  const [newImages, setNewImages] = useState<File[]>([]); // 새로 추가할 이미지

  // 게시글 데이터 불러오기
  useEffect(() => {
    const fetchPost = async () => {
      try {
        const res = await fetchApi(`/api/posts/${id}`, { method: 'GET' });
        const post: PostDto = res.data;
        setTitle(post.title);
        setContent(post.content);
        setExistingImages(post.images || []);
      } catch (err: unknown) {
        if (err instanceof Error) {
          console.error(err.message);
          alert('게시글을 불러오는 중 오류가 발생했습니다.');
        } else {
          console.error(err);
          alert('게시글을 불러오는 중 알 수 없는 오류가 발생했습니다.');
        }
        router.push('/posts/question');
      }
    };

    if (id) fetchPost();
  }, [id, router]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setNewImages([...newImages, ...Array.from(e.target.files)]);
    }
  };

  const handleRemoveExistingImage = (idx: number) => {
    setExistingImages(existingImages.filter((_, i) => i !== idx));
  };

  const handleRemoveNewImage = (idx: number) => {
    setNewImages(newImages.filter((_, i) => i !== idx));
  };

  const handleSubmit = async () => {
    if (!title || !content) {
      alert('제목과 내용을 입력해주세요.');
      return;
    }

    try {
      // 1. 새 이미지를 S3에 업로드
      let newImageUrls: string[] = [];
      if (newImages.length > 0) {
        newImageUrls = await uploadImages(newImages, 'post');
      }

      // 2. 기존 이미지 + 새 이미지 URL 병합
      const allImageUrls = [...existingImages, ...newImageUrls];

      // 3. JSON으로 데이터 전송
      await fetchApi(`/api/posts/${id}`, {
        method: 'PATCH',
        body: JSON.stringify({
          title,
          content,
          imageUrls: allImageUrls
        })
      });

      alert('게시글이 수정되었습니다.');
      router.push('/posts/question');
    } catch (err) {
      console.error(err);
      alert('게시글 수정 중 오류가 발생했습니다.');
    }
  };

  return (
    <div className="p-4 max-w-lg mx-auto">
      <input
        type="text"
        placeholder="제목"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        className="w-full p-2 mb-4 border"
      />

      <div className="mb-4">
        <input type="file" multiple accept="image/*" onChange={handleFileChange} />

        <div className="flex mt-2 gap-2 overflow-x-auto">
          {existingImages.map((img, idx) => (
            <div key={`existing-${idx}`} className="relative">
              <img src={img} alt="existing" className="w-24 h-24 object-cover" />
              <button
                type="button"
                onClick={() => handleRemoveExistingImage(idx)}
                className="absolute top-0 right-0 bg-red-500 text-white rounded-full w-6 h-6 text-sm flex items-center justify-center"
              >
                ×
              </button>
            </div>
          ))}
          {newImages.map((img, idx) => (
            <div key={`new-${idx}`} className="relative">
              <img src={URL.createObjectURL(img)} alt="new" className="w-24 h-24 object-cover" />
              <button
                type="button"
                onClick={() => handleRemoveNewImage(idx)}
                className="absolute top-0 right-0 bg-red-500 text-white rounded-full w-6 h-6 text-sm flex items-center justify-center"
              >
                ×
              </button>
            </div>
          ))}
        </div>
      </div>

      <textarea
        placeholder="게시글 내용"
        value={content}
        onChange={(e) => setContent(e.target.value)}
        className="w-full p-2 mb-4 border h-32"
      />

      <button onClick={handleSubmit} className="px-4 py-2 bg-blue-500 text-white">
        수정
      </button>
    </div>
  );
}