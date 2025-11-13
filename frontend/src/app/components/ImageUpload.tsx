'use client';

import { useState, useRef } from 'react';
import { fetchApi } from '@/lib/client';

interface ImageUploadProps {
  onImageUpload: (file: File) => void;
  directory: string;
  multiple?: boolean;
  maxFiles?: number;
  className?: string;
}

export default function ImageUpload({ 
  onImageUpload, 
  directory, 
  multiple = false, 
  maxFiles = 5,
  className = '' 
}: ImageUploadProps) {
  const [isUploading, setIsUploading] = useState(false);
  const [uploadedImages, setUploadedImages] = useState<string[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileSelect = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = event.target.files;
    if (!files || files.length === 0) return;

    // 파일 개수 제한
    if (files.length > maxFiles) {
      alert(`최대 ${maxFiles}개의 파일만 업로드할 수 있습니다.`);
      return;
    }

    setIsUploading(true);

    try {
      if (multiple) {
        // 다중 파일 업로드는 거래 게시글 등에서 사용
        // 현재는 단일 파일 업로드만 지원
        alert('현재는 단일 파일 업로드만 지원됩니다.');
        return;
      } else {
        // 단일 파일 업로드
        const formData = new FormData();
        formData.append('file', files[0]);
        formData.append('directory', directory);

        // 이미지는 회원가입/수정 시 함께 전송되므로 여기서는 미리보기만 처리
        const file = files[0];
        const imageUrl = URL.createObjectURL(file);
        // 단일 파일이므로 기존 이미지를 대체
        setUploadedImages([imageUrl]);
        onImageUpload(file);
      }
    } catch (error) {
      console.error('이미지 업로드 실패:', error);
      alert('이미지 업로드에 실패했습니다.');
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const removeImage = (index: number) => {
    setUploadedImages(prev => prev.filter((_, i) => i !== index));
  };

  return (
    <div className={`space-y-4 ${className}`}>
      {/* 파일 선택 버튼 */}
      <div>
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          multiple={multiple}
          onChange={handleFileSelect}
          className="hidden"
        />
        <button
          type="button"
          onClick={() => fileInputRef.current?.click()}
          disabled={isUploading}
          className="w-full px-4 py-2 border-2 border-dashed border-gray-300 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-colors disabled:opacity-50"
        >
          {isUploading ? (
            <div className="flex items-center justify-center">
              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600 mr-2"></div>
              업로드 중...
            </div>
          ) : (
            <div className="text-center">
              <svg className="mx-auto h-8 w-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              <p className="mt-2 text-sm text-gray-600">
                {multiple ? '이미지를 선택하세요 (최대 5개)' : '이미지를 선택하세요'}
              </p>
              <p className="text-xs text-gray-500">JPG, PNG, GIF, WEBP (최대 5MB)</p>
            </div>
          )}
        </button>
      </div>

      {/* 업로드된 이미지 미리보기 */}
      {uploadedImages.length > 0 && (
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
          {uploadedImages.map((imageUrl, index) => (
            <div key={index} className="relative group">
              <img
                src={imageUrl}
                alt={`업로드된 이미지 ${index + 1}`}
                className="w-full h-24 object-cover rounded-lg border"
              />
              <button
                type="button"
                onClick={() => removeImage(index)}
                className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-sm hover:bg-red-600 opacity-0 group-hover:opacity-100 transition-opacity"
              >
                ×
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
