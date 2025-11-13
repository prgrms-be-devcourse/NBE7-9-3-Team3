'use client';

import { useState } from 'react';
import { fetchApi } from "@/lib/client";
import { useRouter } from 'next/navigation';
import { uploadImages } from '@/lib/uploadImage';



export default function PostForm() {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [images, setImages] = useState<File[]>([]);
  const router = useRouter();

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setImages([...images, ...Array.from(e.target.files)]);
    }
  };

  const handleRemoveImage = (idx: number) => {
    setImages(images.filter((_, i) => i !== idx));
  };

  const handleSubmit = async () => {
    if (!title || !content) {
      alert('제목과 내용을 입력해주세요.');
      return;
    }

    if (images.length === 0) {
      alert('최소 1개의 이미지를 등록해주세요.');
      return;
    }

    try {
      // 1. S3에 이미지 업로드
      const imageUrls = await uploadImages(images, 'post');

      // 2. JSON으로 데이터 전송
      await fetchApi('/api/posts', {
        method: 'POST',
        body: JSON.stringify({
          title,
          content,
          boardType: 'SHOWOFF',
          imageUrls
        })
      });

      alert('게시글이 생성되었습니다.');
      setTitle('');
      setContent('');
      setImages([]);

      router.push('/posts/showoff');
    } catch (err) {
      console.error(err);
      alert('게시글 생성 중 오류가 발생했습니다.');
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
          {images.map((img, idx) => (
            <div key={idx} className="relative">
              <img
                src={URL.createObjectURL(img)}
                alt="preview"
                className="w-24 h-24 object-cover"
              />
              <button
                type="button"
                onClick={() => handleRemoveImage(idx)}
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
      <button
        onClick={handleSubmit}
        className="px-4 py-2 bg-blue-500 text-white"
      >
        게시
      </button>
    </div>
  );
}