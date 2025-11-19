// 리프레시 토큰으로 새 액세스 토큰 발급
async function refreshAccessToken(): Promise<boolean> {
  try {
    const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;
    const response = await fetch(`${baseUrl}/api/members/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      // body 없이 요청 (쿠키에서 리프레시 토큰 읽음)
    });
    
    if (response.ok) {
      // 새 액세스 토큰과 리프레시 토큰이 쿠키에 자동으로 설정됨
      return true;
    }
    
    return false;
  } catch (error) {
    console.error('토큰 갱신 실패:', error);
    return false;
  }
}

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
      // 401 에러 발생 시 리프레시 토큰으로 새 액세스 토큰 발급 시도
      if (res.status === 401 && url !== '/api/members/refresh') {
        const refreshed = await refreshAccessToken();
        
        if (refreshed) {
          // 새 토큰으로 원래 요청 재시도
          return fetch(`${baseUrl}${url}`, newOptions).then(
            async (retryRes) => {
              if (!retryRes.ok) {
                const contentType = retryRes.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                  const rsData = await retryRes.json();
                  throw new Error(rsData.msg || "요청 실패");
                } else {
                  throw new Error(`HTTP ${retryRes.status}: ${retryRes.statusText}`);
                }
              }
              if (retryRes.status === 204) {
                return null;
              }
              const contentType = retryRes.headers.get('content-type');
              if (contentType && contentType.includes('application/json')) {
                return retryRes.json();
              } else {
                const text = await retryRes.text();
                console.warn('서버에서 JSON이 아닌 응답을 받았습니다:', text);
                throw new Error('서버에서 유효하지 않은 응답을 받았습니다.');
              }
            }
          );
        } else {
          // 리프레시 토큰도 만료된 경우
          // /api/members/me 호출 시에는 리다이렉트하지 않음 (무한 루프 방지)
          const isMeEndpoint = url === '/api/members/me';
          
          if (!isMeEndpoint) {
            // 리다이렉트 플래그 확인 (무한 루프 방지)
            if (typeof window !== 'undefined') {
              const redirecting = sessionStorage.getItem('auth_redirecting');
              if (redirecting === 'true') {
                // 이미 리다이렉트 중이면 에러만 던짐
                throw new Error("인증이 만료되었습니다. 다시 로그인해주세요.");
              }
              
              // 리다이렉트 플래그 설정
              sessionStorage.setItem('auth_redirecting', 'true');
              
              console.log('리프레시 토큰이 만료되어 로그아웃합니다.');
              
              // 로그아웃 API 호출 (실패해도 무시)
              try {
                await fetch(`${baseUrl}/api/members/logout`, {
                  method: 'POST',
                  credentials: 'include',
                });
              } catch (error) {
                // 로그아웃 API 실패는 무시 (이미 토큰이 만료되었을 수 있음)
                console.log('로그아웃 API 호출 실패 (무시됨)');
              }
              
              // 메인 페이지로 리다이렉트
              window.location.href = '/';
            }
          }
          
          // 에러를 던져서 원래 요청이 실패하도록 함
          throw new Error("인증이 만료되었습니다. 다시 로그인해주세요.");
        }
      }
      
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