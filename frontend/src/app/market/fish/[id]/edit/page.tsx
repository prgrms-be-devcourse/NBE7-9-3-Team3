'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import { TradeFormData, TradeStatus, ApiResponse, Trade } from '@/type/trade';
import { fetchApi } from '@/lib/client';
import { useAuth } from '@/context/AuthContext';
import { uploadImages } from '@/lib/uploadImage';

export default function FishEditPage() {
  const router = useRouter();
  const params = useParams();
  const tradeId = params.id as string;
  const { user, isAuthenticated, loading: authLoading } = useAuth();

  const [formData, setFormData] = useState<TradeFormData>({
    memberId: 0, // Will be set from auth context
    title: '',
    description: '',
    price: 0,
    status: TradeStatus.SELLING,
    category: '',
  });
  const [images, setImages] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);
  const [existingImages, setExistingImages] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  // Check authentication
  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      alert('로그인이 필요합니다.');
      router.push('/login');
    }
  }, [authLoading, isAuthenticated, router]);

  useEffect(() => {
    if (tradeId && user) {
      fetchPost();
    }
  }, [tradeId, user]);

  const fetchPost = async () => {
    try {
      const data: ApiResponse<Trade> = await fetchApi(`/api/market/fish/${tradeId}`);
      if (data.resultCode.startsWith('200')) {
        const post = data.data;
        setFormData({
          memberId: user?.memberId || 0, // Use current logged-in user's ID
          title: post.title,
          description: post.description,
          price: post.price,
          status: post.status,
          category: post.category || '',
        });
        setExistingImages(post.images || []);
      }
    } catch (error) {
      console.error('Failed to load post:', error);
      alert('게시글을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'price' ? Number(value) : value,
    }));
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const files = Array.from(e.target.files);
      setImages(files);

      // Create preview URLs
      const previewUrls = files.map((file) => URL.createObjectURL(file));
      setPreviews(previewUrls);
    }
  };

  const removeNewImage = (index: number) => {
    const newImages = images.filter((_, i) => i !== index);
    const newPreviews = previews.filter((_, i) => i !== index);
    setImages(newImages);
    setPreviews(newPreviews);
  };

  const removeExistingImage = (index: number) => {
    setExistingImages(existingImages.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      // 1. Upload new images to S3 using Presigned URLs
      let newImageUrls: string[] = [];
      if (images.length > 0) {
        newImageUrls = await uploadImages(images, 'trade');
      }

      // 2. Combine existing images and new images
      const allImageUrls = [...existingImages, ...newImageUrls];

      // 3. Send JSON data with image URLs
      const requestData = {
        ...formData,
        imageUrls: allImageUrls
      };

      const data: ApiResponse<Trade> = await fetchApi(`/api/market/fish/${tradeId}`, {
        method: 'PUT',
        body: JSON.stringify(requestData)
      });

      if (data.resultCode.startsWith('200')) {
        alert('게시글이 수정되었습니다!');
        router.push(`/market/fish/${tradeId}`);
      } else {
        alert('게시글 수정 실패: ' + data.msg);
      }
    } catch (error) {
      console.error('Failed to update post:', error);
      alert('게시글 수정 실패: ' + (error instanceof Error ? error.message : 'Unknown error'));
    } finally {
      setSubmitting(false);
    }
  };

  if (authLoading || loading) {
    return <div className="p-8">로딩 중...</div>;
  }

  // Don't render form if not authenticated
  if (!isAuthenticated || !user) {
    return null;
  }

  return (
    <div className="max-w-4xl mx-auto p-8">
      <div className="mb-6">
        <Link href={`/market/fish/${tradeId}`} className="text-blue-500 hover:underline">
          ← 뒤로가기
        </Link>
      </div>

      <h1 className="text-3xl font-bold mb-8">물고기 거래 글 수정</h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* 제목 */}
        <div className="bg-gray-50 p-6 rounded-lg">
          <label className="block text-sm font-semibold mb-3 flex items-center">
            <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
            제목
          </label>
          <input
            type="text"
            name="title"
            value={formData.title}
            onChange={handleChange}
            required
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="제목을 입력하세요"
          />
        </div>

        {/* 이미지 업로드 */}
        <div className="bg-gray-50 p-6 rounded-lg">
          <label className="block text-sm font-semibold mb-3 flex items-center">
            <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
            이미지 수정
          </label>

          <div className="grid grid-cols-4 gap-4 mb-4">
            {/* 기존 이미지 */}
            {existingImages.map((image, index) => (
              <div key={`existing-${index}`} className="relative group">
                <img
                  src={image}
                  alt={`Existing ${index + 1}`}
                  className="w-full h-32 object-cover rounded-lg border"
                />
                <button
                  type="button"
                  onClick={() => removeExistingImage(index)}
                  className="absolute top-2 right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center opacity-0 group-hover:opacity-100 transition"
                >
                  ×
                </button>
                <div className="absolute bottom-2 left-2 bg-blue-500 text-white text-xs px-2 py-1 rounded">
                  기존
                </div>
              </div>
            ))}

            {/* 새 이미지 미리보기 */}
            {previews.map((preview, index) => (
              <div key={`new-${index}`} className="relative group">
                <img
                  src={preview}
                  alt={`Preview ${index + 1}`}
                  className="w-full h-32 object-cover rounded-lg border"
                />
                <button
                  type="button"
                  onClick={() => removeNewImage(index)}
                  className="absolute top-2 right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center opacity-0 group-hover:opacity-100 transition"
                >
                  ×
                </button>
                <div className="absolute bottom-2 left-2 bg-green-500 text-white text-xs px-2 py-1 rounded">
                  새로운
                </div>
              </div>
            ))}

            {/* 이미지 추가 버튼 */}
            <label className="border-2 border-dashed border-gray-300 rounded-lg h-32 flex flex-col items-center justify-center cursor-pointer hover:border-blue-500 hover:bg-blue-50 transition">
              <div className="text-4xl text-gray-400 mb-1">+</div>
              <span className="text-sm text-gray-500">이미지 추가</span>
              <input
                type="file"
                accept="image/*"
                multiple
                onChange={handleImageChange}
                className="hidden"
              />
            </label>
          </div>
          <p className="text-xs text-gray-500 mt-2">
            * 최대 5개, 각 파일은 5MB 이하, jpg/jpeg/png/gif/webp만 가능
          </p>
        </div>

        {/* 가격 */}
        <div className="bg-gray-50 p-6 rounded-lg">
          <label className="block text-sm font-semibold mb-3 flex items-center">
            <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
            가격
          </label>
          <div className="flex items-center gap-2">
            <input
              type="number"
              name="price"
              value={formData.price}
              onChange={handleChange}
              required
              min="0"
              className="flex-1 p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="0원 입력 시 나눔 게시글로 등록됩니다"
            />
            <span className="text-gray-600 font-medium">원</span>
          </div>
        </div>

        {/* 카테고리 (선택) */}
        <div className="bg-gray-50 p-6 rounded-lg">
          <label className="block text-sm font-semibold mb-3 flex items-center">
            <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
            태그 (선택)
          </label>
          <input
            type="text"
            name="category"
            value={formData.category}
            onChange={handleChange}
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* 설명 */}
        <div className="bg-gray-50 p-6 rounded-lg">
          <label className="block text-sm font-semibold mb-3 flex items-center">
            <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
            설명
          </label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleChange}
            required
            className="w-full p-3 border border-gray-300 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows={8}
            placeholder="상품에 대한 자세한 설명을 입력하세요&#10;&#10;예시:&#10;- 어종: 광어&#10;- 중량: 2kg&#10;- 수확 시기: 오늘 오전&#10;- 거래 방법: 직거래 or 택배"
          />
          <p className="text-xs text-gray-500 mt-2">
            * 상품 상태, 거래 방법, 거래 장소 등을 자세히 적어주세요
          </p>
        </div>

        {/* 상태 (판매중/판매완료) */}
        <div className="bg-gray-50 p-6 rounded-lg">
          <label className="block text-sm font-semibold mb-3 flex items-center">
            <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
            판매 상태
          </label>
          <select
            name="status"
            value={formData.status}
            onChange={handleChange}
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value={TradeStatus.SELLING}>판매중</option>
            <option value={TradeStatus.COMPLETED}>판매완료</option>
            <option value={TradeStatus.CANCELLED}>판매취소</option>
          </select>
        </div>

        {/* 버튼 */}
        <div className="flex gap-4 pt-4">
          <button
            type="submit"
            disabled={submitting}
            className="flex-1 px-6 py-4 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-400 font-semibold text-lg transition"
          >
            {submitting ? '수정 중...' : '수정하기'}
          </button>
          <Link
            href={`/market/fish/${tradeId}`}
            className="px-6 py-4 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 font-semibold text-lg transition flex items-center justify-center"
          >
            취소
          </Link>
        </div>
      </form>
    </div>
  );
}
