import { fetchApi } from './client';

// Presigned URL 응답 타입
interface PresignedURLResponse {
  presignedUrl: string;
  fileUrl : string;
}

// 파일 검증 상수
const ALLOWED_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif', 'webp'];
const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

/** 파일 유효성 검증 */
function validateFile(file: File): void {
  // 파일 크기 검증
  if (file.size > MAX_FILE_SIZE) {
    throw new Error('파일 크기는 5MB를 초과할 수 없습니다.');
  }

  // 파일 확장자 검증
  const extension = file.name.split('.').pop()?.toLowerCase();
  if (!extension || !ALLOWED_EXTENSIONS.includes(extension)) {
    throw new Error(`허용되지 않는 파일 형식입니다. (허용: ${ALLOWED_EXTENSIONS.join(', ')})`);
  }
}

/** 1단계: 백엔드에서 Presigned URL 요청 */
async function getPresignedUrl(
    fileName: string,
    directory: string
): Promise<PresignedURLResponse> {
  const response = await fetchApi('/api/images/upload', {
    method: 'POST',
    body: JSON.stringify({ fileName, directory })
  });
  return response.data;
}

/** 2단계: Presigned URL로 S3에 직접 업로드 */
async function uploadToS3(presignedUrl: string, file: File): Promise<void> {
  const response = await fetch(presignedUrl, {
    method: 'PUT',
    body: file,
    headers: {
      'Content-Type': file.type
    }
  });

  if (!response.ok) {
    throw new Error('S3 업로드 실패');
  }
}

/**
 * 이미지 업로드 전체 프로세스 (메인 함수)
 * @param file - 업로드할 파일
 * @param directory - S3 저장 경로 (예: 'trade', 'profile')
 * @returns 최종 S3 URL (DB 저장용)
 */
export async function uploadImage(
    file: File,
    directory: string
): Promise<string> {
  // 파일 검증
  validateFile(file);

  const { presignedUrl, fileUrl } = await getPresignedUrl(file.name, directory);
  await uploadToS3(presignedUrl, file);
  return fileUrl;
}

/** 여러 이미지 한번에 업로드 */
export async function uploadImages(
    files: File[],
    directory: string
): Promise<string[]> {
  const uploadPromises = files.map(file => uploadImage(file, directory));
  return Promise.all(uploadPromises);
}