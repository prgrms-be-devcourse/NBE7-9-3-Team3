'use client';

import { useEffect, useState } from 'react';
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

// 어항 데이터 타입
interface Aquarium {
  aquariumId: number;
  aquariumName: string;
  createDate: string;
  notifyCycleDate?: number;
  lastNotifyDate: string;
  nextNotifyDate: string;
}

export default function AquariumsPage() {
  const { isAuthenticated, loading: authLoading } = useAuth();
  const router = useRouter();
  
  const [aquariums, setAquariums] = useState<Aquarium[]>([]);
  const [newAquariumName, setNewAquariumName] = useState('');
  const [isAdding, setIsAdding] = useState(false);  // "어항 추가" 버튼 클릭 여부 확인
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [myFishesAquariumId, setMyFishesAquariumId] = useState<number | null>(null);
  const [hasMyFishes, setHasMyFishes] = useState(false);
  const [editingAquariumId, setEditingAquariumId] = useState<number | null>(null);
  const [editingAquariumName, setEditingAquariumName] = useState('');

  // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, authLoading, router]);

  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;

  // "내가 키운 물고기" 어항을 항상 맨 위에 정렬
  const sortAquariums = (list: Aquarium[]) => {
    return [...list].sort((a, b) => {
      if (a.aquariumName === "내가 키운 물고기") return -1;
      if (b.aquariumName === "내가 키운 물고기") return 1;
      return 0;
    });
  };

  // 어항 이름 수정
  const startEditAquarium = (aquariumId: number, currentName: string) => {
    setEditingAquariumId(aquariumId);
    setEditingAquariumName(currentName);
  };

  const cancelEditAquarium = () => {
    setEditingAquariumId(null);
    setEditingAquariumName('');
  };

  const confirmEditAquarium = async (aquariumId: number) => {
    const trimmed = editingAquariumName.trim();
    if (trimmed.length === 0) {
      alert('어항 이름을 입력해주세요 :)');
      return;
    }
    if (trimmed === '내가 키운 물고기') {
      alert('"내가 키운 물고기"는 어항 이름으로 사용할 수 없습니다.');
      return;
    }

    try {
      const res = await fetch(`${baseUrl}/api/aquarium/${aquariumId}`, {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ aquariumName: trimmed })
      });
      const json = await res.json();
      // 상태 갱신 및 정렬 유지
      setAquariums(prev => sortAquariums(prev.map(a => (
        a.aquariumId === aquariumId ? { ...a, aquariumName: trimmed } : a
      ))));
      setEditingAquariumId(null);
      setEditingAquariumName('');
    } catch (err) {
      console.error('어항 수정 중 오류:', err);
      alert('어항 수정 중 오류가 발생했습니다.');
    }
  };

  // 어항 단건 조회 페이지로 이동
  const handleDetails  = (id: number) => {
    router.push(`/aquarium/${id}`);
  };

  // "내가 키운 물고기" 어항 단건 조회 페이지로 이동
  const goToMyfishesPage = (id: number) => {
    router.push(`/aquarium/${id}/myfishes`);
  };

  // 어항 목록 (다건 조회)
  useEffect(() => {
    if (!isAuthenticated) return;
    
    fetch(`${baseUrl}/api/aquarium`, {
      credentials: 'include',
    })
      .then(res => res.json())
      .then(json => {
        const sortedAquariums = sortAquariums(json.data);
        setAquariums(sortedAquariums);
        
        // "내가 키운 물고기" 어항 찾기
        const myFishesAquarium = sortedAquariums.find(a => a.aquariumName === "내가 키운 물고기");
        if (myFishesAquarium) {
          setMyFishesAquariumId(myFishesAquarium.aquariumId);
          // 해당 어항에 물고기가 있는지 확인
          checkMyFishesCount(myFishesAquarium.aquariumId);
        } else {
          setMyFishesAquariumId(null);
          setHasMyFishes(false);
        }
      })
      .catch(err => {
        console.error(err);
        // 인증 오류인 경우 로그인 페이지로 리다이렉트
        if (err.message && (err.message.includes('CMN006') || err.message.includes('인증이 필요합니다'))) {
          window.location.href = '/login';
        }
      });
  }, [refreshTrigger, isAuthenticated]); // refreshTrigger가 변경될 때마다 다시 조회

  // "내가 키운 물고기" 어항의 물고기 개수 확인
  const checkMyFishesCount = async (aquariumId: number) => {
    try {
      const response = await fetch(`${baseUrl}/api/aquarium/${aquariumId}/fish`, {
        credentials: 'include',
      });
      const json = await response.json();
      setHasMyFishes(json.data && json.data.length > 0);
    } catch (err) {
      console.error('물고기 개수 확인 중 오류:', err);
      setHasMyFishes(false);
    }
  };

  // 새 어항 추가
  const handleAddAquarium = () => {
    setIsAdding(true);
  };

  const handleCancel = () => {
    setIsAdding(false);
    setNewAquariumName('');
  };

  const handleConfirm = () => {
    if (newAquariumName.trim() === '') {
      alert('어항 이름을 입력해주세요 :)');
      return;
    }
    if (newAquariumName === '내가 키운 물고기') {
      alert('"내가 키운 물고기"는 어항 이름으로 사용할 수 없습니다.');
      return;
    }

    fetch(`${baseUrl}/api/aquarium`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ aquariumName: newAquariumName }),
    })
      .then(res => res.json())
      .then(json => {
        setAquariums(prev => sortAquariums([json.data, ...prev]));
        setNewAquariumName('');
        setIsAdding(false);
      })
      .catch(err => {
        console.error(err);
        alert('어항 추가 중 오류가 발생했습니다.');
      });
  };

  // 어항 삭제
  const handleDelete = async (id: number) => {
    try {
      // 어항 속 물고기 존재 여부 확인
      const response = await fetch(`${baseUrl}/api/aquarium/${id}/delete`, {
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
      });

      const responseData = await response.json();
  
      // 물고기 존재 시, "내가 키운 물고기" 어항으로 물고기 이동 여부 확인
      // 물고기 이동에 동의한다면, "내가 키운 물고기"로 물고기 이동시킨 후, 해당 어항 삭제
      // 물고기 이동에 동의하지 않는다면, 물고기 이동X, 어항 삭제X
      if (responseData.data === true) {
        const confirmMove = window.confirm(
          '어항에 물고기가 존재합니다 🐟 🐡\n물고기를 "내가 키운 물고기" 어항으로 이동 후, 삭제하시겠습니까?'
        );
        
        if (!confirmMove) {
          return;
        }

        const fishMove = await fetch(`${baseUrl}/api/aquarium/${id}/delete`, {
          credentials: 'include',
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
        });

        const fishMoveResponseData = await fishMove.json();

        if(fishMoveResponseData.data === "물고기 이동 완료") {
          await fetch(`${baseUrl}/api/aquarium/${id}/delete`, {
            method: 'DELETE',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
          });

          alert('어항이 삭제되었습니다 :)');

          // 해당 어항 제거
          setAquariums(prev => {
            const filtered = prev.filter(a => a.aquariumId !== id);
            console.log('Updated aquariums after fish move:', filtered);
            return sortAquariums(filtered);
          });
          
          // 강제 리렌더링 트리거
          setRefreshTrigger(prev => prev + 1);
        }

      }

      // 물고기 존재 안할 시, 해당 어항 바로 삭제 
      else if (responseData.data === false) {
        await fetch(`${baseUrl}/api/aquarium/${id}/delete`, {
          method: 'DELETE',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
        });
        
        alert('어항이 삭제되었습니다 :)');

        // 해당 어항 제거
        setAquariums(prev => {
          const filtered = prev.filter(a => a.aquariumId !== id);
          console.log('Updated aquariums after direct delete:', filtered);
          return sortAquariums(filtered);
        });
        
        // 강제 리렌더링 트리거
        setRefreshTrigger(prev => prev + 1);
      }

    } catch (err) {
      console.error('삭제 요청 중 오류 발생:', err);
      alert('어항 삭제 중 오류가 발생했습니다.');
    }
  };

  // 어항 알림 스케줄 설정
  const handleNotificationSettings = async (id: number) => {
    const input = window.prompt(
      '⏰ 알림 주기를 일 단위로 입력해주세요. 해당 주기마다 메일이 보내집니다!\n알림을 설정하고 싶지 않다면, 0을 입력해주세요.'
    );
  
    if (input === null) {
      return;
    }
  
    let cycleDate = parseInt(input);
    if (isNaN(cycleDate) || cycleDate < 0) {
      alert('0 또는 양의 숫자를 입력해주세요.');
      return;
    }
  
    try {
      const response = await fetch(`${baseUrl}/api/aquarium/${id}/schedule`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ cycleDate: cycleDate }),
      });
  
      const responseData = await response.json();
  
      if (responseData.resultCode === "200") {
        alert(`${cycleDate}일에 한번씩 메일이 보내집니다 📫`);
        
        // notifyCycleDate 상태 업데이트
        setAquariums(prev => prev.map(a => 
          a.aquariumId === id ? { ...a, notifyCycleDate: cycleDate } : a
        ));
      } else {
        alert('알림 설정에 실패했습니다.');
      }
    } catch (err) {
      console.error('알림 설정 중 오류 발생:', err);
      alert('알림 설정 중 오류가 발생했습니다.');
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

        {/* 섹션 헤더 */}
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-2xl font-bold">🫧 내 어항 목록</h1>
          <button
            onClick={handleAddAquarium}
            className="bg-purple-200 hover:bg-purple-300 text-black px-5 py-2 rounded-4xl flex items-center space-x-2 transition-colors">
            <span className="text-xl">+</span>
            <span>어항 추가</span>
          </button>
        </div>

        {/* 어항 추가를 위한 입력창 */}
        {isAdding && (
          <div className="mb-8 p-4 border border-gray-200 rounded-lg bg-gray-50">
            <label className="block text-gray-700 font-medium mb-2">
              새 어항 이름 입력
            </label>
            <input
              type="text"
              value={newAquariumName}
              onChange={(e) => setNewAquariumName(e.target.value)}
              placeholder="예: 나의 작은 물고기들"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 mb-3 focus:outline-none focus:ring focus:ring-purple-200"
            />
            <div className="flex space-x-2">
              <button
                onClick={handleConfirm}
                className="bg-blue-100 hover:bg-blue-200 text-blue-800 px-4 py-2 rounded-lg text-sm transition-colors">
                추가
              </button>
              <button
                onClick={handleCancel}
                className="bg-gray-100 hover:bg-gray-200 text-gray-800 px-4 py-2 rounded-lg text-sm transition-colors">
                취소
              </button>
            </div>
          </div>
        )}

          {/* 어항 목록 */}
          <div className="space-y-6" key={refreshTrigger}>
          {aquariums.map((aquarium) => {
            // "내가 키운 물고기" 어항이지만 물고기가 없으면 렌더링하지 않음
            if (aquarium.aquariumName === "내가 키운 물고기" && !hasMyFishes) {
              return null;
            }
            
            return (
            <div key={aquarium.aquariumId} className="bg-white border border-gray-200 rounded-lg p-6">

              {/* "내가 키운 물고기" 어항 */}
              {aquarium.aquariumName === "내가 키운 물고기" ? (
                <div className="flex justify-between items-center">
                  <h3 className="text-lg font-medium text-gray-900">내가 키운 물고기</h3>
                  <button
                    onClick={() => goToMyfishesPage(aquarium.aquariumId)}
                    className="bg-blue-100 hover:bg-blue-200 text-blue-800 px-4 py-2 rounded-lg text-sm transition-colors"
                  >
                    자세히
                  </button>
                </div>
              ) : (
                <>
                  {/* 어항이름 및 자세히, 수정, 삭제 버튼 */}
                  <div className="flex justify-between items-center mb-4">
                    {editingAquariumId === aquarium.aquariumId ? (
                      <div className="flex items-center gap-2">
                        <input
                          type="text"
                          value={editingAquariumName}
                          onChange={(e) => setEditingAquariumName(e.target.value)}
                          className="border border-gray-300 rounded px-3 py-2 text-sm"
                        />
                        <button
                          onClick={() => confirmEditAquarium(aquarium.aquariumId)}
                          className="bg-blue-100 hover:bg-blue-200 text-blue-800 px-3 py-2 rounded-lg text-sm transition-colors"
                        >
                          확인
                        </button>
                        <button
                          onClick={cancelEditAquarium}
                          className="bg-gray-100 hover:bg-gray-200 text-gray-800 px-3 py-2 rounded-lg text-sm transition-colors"
                        >
                          취소
                        </button>
                      </div>
                    ) : (
                      <h3 className="text-lg font-medium text-gray-900">{aquarium.aquariumName}</h3>
                    )}
                    <div className="flex space-x-2">
                      <button
                        onClick={() => handleDetails(aquarium.aquariumId)}
                        className="bg-blue-100 hover:bg-blue-200 text-blue-800 px-4 py-2 rounded-lg text-sm transition-colors"
                      >
                        자세히
                      </button>
                      {editingAquariumId !== aquarium.aquariumId && (
                        <button
                          onClick={() => startEditAquarium(aquarium.aquariumId, aquarium.aquariumName)}
                          className="bg-yellow-100 hover:bg-yellow-200 text-yellow-800 px-4 py-2 rounded-lg text-sm transition-colors"
                        >
                          수정
                        </button>
                      )}
                      <button
                        onClick={() => handleDelete(aquarium.aquariumId)}
                        className="bg-gray-100 hover:bg-gray-200 text-gray-800 px-4 py-2 rounded-lg text-sm transition-colors"
                      >
                        삭제
                      </button>
                    </div>
                  </div>

                  {/* 물갈이&어항 세척 주기 span, 알림설정 버튼 */}
                  <div className="flex justify-between items-center">
                    <div className="flex items-center space-x-2">
                      <span className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm">
                        물갈이 & 어항세척 주기
                      </span>
                      <span className="bg-purple-100 text-purple-800 px-3 py-1 rounded-full text-sm">
                        {aquarium.notifyCycleDate && aquarium.notifyCycleDate > 0 ? `${aquarium.notifyCycleDate} days` : '미설정'}
                      </span>
                    </div>
                    <button
                      onClick={() => handleNotificationSettings(aquarium.aquariumId)}
                      className="bg-green-100 hover:bg-green-200 text-green-800 px-4 py-2 rounded-lg text-sm transition-colors"
                    >
                      알림 설정
                    </button>
                  </div>
                </>
              )}
            </div>
            );
          })}
        </div>
      </main>
    </div>
  );
}
