'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useAuth } from "@/context/AuthContext";

// 물고기 데이터 타입
interface Fish {
  fishId: number;
  fishSpecies: string;
  fishName: string;
}

// 물고기 상태 타입
interface FishStatus {
  logId: number;
  aquariumId: number;
  fishId: number;
  status: string;
  logDate: string;
}

export default function MyFishesPage() {
  const { isAuthenticated, loading: authLoading } = useAuth();
  const params = useParams();
  const router = useRouter();
  const aquariumId = params.id as string;
  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;

  // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, authLoading, router]);

  const [fishes, setFishes] = useState<Fish[]>([]);
  const [fishStatuses, setFishStatuses] = useState<FishStatus[]>([]);
  const [expandedFish, setExpandedFish] = useState<{ [key: number]: boolean }>({});

  // 물고기 다건 조회
  useEffect(() => {
    if (!isAuthenticated) return;
    
    fetch(`${baseUrl}/api/aquarium/${aquariumId}/fish`, {
      credentials: 'include',
    })
      .then(res => res.json())
      .then(json => setFishes(json.data))
      .catch(err => {
        console.error(err);
        // 인증 오류인 경우 로그인 페이지로 리다이렉트
        if (err.message && (err.message.includes('CMN006') || err.message.includes('인증이 필요합니다'))) {
          window.location.href = '/login';
        }
      });
  }, [aquariumId, isAuthenticated]);

  // 물고기 상태 조회 (모든 물고기의 상태를 조회)
  useEffect(() => {
    if (!isAuthenticated || fishes.length === 0) return; // 물고기가 없으면 조회하지 않음
    
    const fetchAllFishStatuses = async () => {
      try {
        const statusPromises = fishes.map(fish => 
          fetch(`${baseUrl}/api/fish/${fish.fishId}/fishLog`, {
            credentials: 'include',
          }).then(res => res.json())
        );
        
        const allStatuses = await Promise.all(statusPromises);
        // ApiResponse 구조에서 data 추출하여 각 물고기별로 반환된 배열들을 하나로 합치고 날짜 최신순으로 정렬
        const flatStatuses = allStatuses
            .map(response => response?.data || [])
            .flat()
            .sort((a, b) => new Date(b.logDate).getTime() - new Date(a.logDate).getTime())
        setFishStatuses(flatStatuses);
      } catch (err) {
        console.error('Fish status fetch error:', err);
        setFishStatuses([]);
      }
    };
    
    fetchAllFishStatuses();
  }, [fishes]); // fishes가 변경될 때마다 실행

  // 물고기 상태 보기/안보기 토글
  const toggleFishStatus = (fishId: number) => {
    setExpandedFish(prev => ({
      ...prev,
      [fishId]: !prev[fishId]
    }));
  };

  // 물고기 삭제
  const deleteFish = (fishId: number) => {
    if (window.confirm('정말로 이 물고기를 삭제하시겠습니까?')) {
      fetch(`${baseUrl}/api/aquarium/${aquariumId}/fish/${fishId}`, {
        method: 'DELETE',
        credentials: 'include',
      })
        .then(res => {
          if (res.ok) {
            setFishes(prev => prev.filter(fish => fish.fishId !== fishId));
            setExpandedFish(prev => {
              const newExpanded = { ...prev };
              delete newExpanded[fishId];
              return newExpanded;
            });
            // 해당 물고기의 상태도 삭제
            setFishStatuses(prev => prev.filter(status => status.fishId !== fishId));
            alert('물고기가 삭제되었습니다.');
            
            // 물고기가 모두 삭제되었으면 aquarium 페이지로 돌아가기
            const remainingFishes = fishes.filter(fish => fish.fishId !== fishId);
            if (remainingFishes.length === 0) {
              alert('모든 물고기가 삭제되었습니다. 어항 목록으로 돌아갑니다.');
              router.push('/aquarium');
            }
          } else {
            alert('삭제 중 오류가 발생했습니다.');
          }
        })
        .catch(err => {
          console.error(err);
          alert('삭제 중 오류가 발생했습니다.');
        });
    }
  };


  // 로딩 중이거나 인증되지 않은 경우
  if (authLoading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <p className="text-center">로딩 중...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <p className="text-center">로그인이 필요합니다.</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white">
      {/* 메인 컨텐츠 */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        
        {/* 헤더 */}
        <div className="flex items-center justify-center mb-8">
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold text-gray-800">🐠 내가 키운 물고기</h1>
          </div>
        </div>

        {/* 물고기 목록 */}
        <div className="space-y-4">
          {fishes && fishes.length > 0 ? (
            fishes.map((fish) => (
            <div key={fish.fishId} className="bg-gray-50 p-4 rounded-lg">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <span className="font-bold text-gray-800">{fish.fishName}</span>
                  <span className="bg-gray-200 text-gray-600 px-2 py-1 rounded-full text-sm font-normal">
                    {fish.fishSpecies}
                  </span>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => toggleFishStatus(fish.fishId)}
                    className="bg-blue-200 text-blue-800 px-3 py-1 rounded-lg text-sm font-normal"
                  >
                    {expandedFish[fish.fishId] ? '상태 닫기' : '상태 보기'}
                  </button>
                  <button
                    onClick={() => deleteFish(fish.fishId)}
                    className="bg-gray-200 text-gray-600 px-3 py-1 rounded-lg text-sm font-normal"
                  >
                    삭제
                  </button>
                </div>
              </div>
              <div className="mt-3 p-3">
                {/* 상태 추가 버튼 제거 */}
              </div>

              {/* 상태 추가 입력창 제거 */}

              {/* 상태 정보 (확장된 경우만 표시) */}
              {expandedFish[fish.fishId] && (
                <div className="mt-3 space-y-1">
                  {fishStatuses && fishStatuses.length > 0 ? (
                    fishStatuses
                      .filter(status => status && status.fishId === fish.fishId)
                      .map((status, index) => (
                        <div key={index} className="flex items-center justify-between text-sm text-gray-600">
                          <div className="flex items-center gap-3">
                            <span className="font-normal">{status.logDate ? new Date(status.logDate).toLocaleDateString('ko-KR') : ''}</span>
                            <span className="font-normal">{status.status || ''}</span>
                          </div>
                          {/* 수정/삭제 버튼 제거 */}
                        </div>
                      ))
                  ) : (
                    <div className="text-sm text-gray-500">등록된 상태가 없습니다.</div>
                  )}
                </div>
              )}
            </div>
            ))
          ) : (
            <div className="text-sm text-gray-500 text-center py-8">
              등록된 물고기가 없습니다.
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
