export function fetchApi(url: string, options?: RequestInit & { headers?: { [key: string]: string } }) {
  const newOptions: RequestInit = { ...options, credentials: 'include' };
  
  // 기존 헤더와 새로 받은 헤더를 병합합니다.
  const headers = new Headers(newOptions.headers || {});
  if (newOptions.body && !(newOptions.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }
  newOptions.headers = headers;

  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;
  return fetch(`${baseUrl}${url}`, newOptions).then(
    async (res) => {
      if (!res.ok) {
        // Content-Type을 확인하여 JSON인지 체크
        const contentType = res.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
          const rsData = await res.json();
          throw new Error(rsData.msg || "요청 실패");
        } else {
          throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        }
      }
      if (res.status === 204) {
        return null; 
      }
      
      // Content-Type을 확인하여 JSON인지 체크
      const contentType = res.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return res.json();
      } else {
        // JSON이 아닌 경우 텍스트로 반환
        const text = await res.text();
        console.warn('서버에서 JSON이 아닌 응답을 받았습니다:', text);
        throw new Error('서버에서 유효하지 않은 응답을 받았습니다.');
      }
    }
  );
}

// 편의를 위한 HTTP 메서드별 함수들
export const api = {
  get: <T>(url: string, options?: RequestInit & { headers?: { [key: string]: string } }) => 
    fetchApi(url, { ...options, method: 'GET' }) as Promise<T>,
  
  post: <T>(url: string, body?: any, options?: RequestInit & { headers?: { [key: string]: string } }) => 
    fetchApi(url, { ...options, method: 'POST', body: body ? JSON.stringify(body) : undefined }) as Promise<T>,
  
  put: <T>(url: string, body?: any, options?: RequestInit & { headers?: { [key: string]: string } }) => 
    fetchApi(url, { ...options, method: 'PUT', body: body ? JSON.stringify(body) : undefined }) as Promise<T>,
  
  delete: <T>(url: string, options?: RequestInit & { headers?: { [key: string]: string } }) => 
    fetchApi(url, { ...options, method: 'DELETE' }) as Promise<T>,
};