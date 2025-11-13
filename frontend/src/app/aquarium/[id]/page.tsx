'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
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

// 어항 로그 (온도 & PH) 타입
interface EnvironmentData {
    logId: number;
    aquariumId: number;
    temperature: number;
    ph: number;
    logDate: string;
}

// 어항 로그 요청 타입
interface AquariumLogRequest {
    aquariumId: number;
    temperature: number;
    ph: number;
    logDate: string;
}

export default function AquariumDetailPage() {
  const { isAuthenticated, loading: authLoading } = useAuth();
  const router = useRouter();
  const params = useParams();
  const aquariumId = params.id as string;
  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;

  // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, authLoading, router]);

  const [aquarium, setAquarium] = useState<Aquarium | null>(null);
  const [fishes, setFishes] = useState<Fish[]>([]);
  const [newFishName, setNewFishName] = useState('');
  const [newFishSpecies, setNewFishSpecies] = useState('');
  const [isAdding, setIsAdding] = useState(false); 
  const [newFishStatus, setNewFishStatus] = useState('');
  const [newFishStatusDate, setNewFishStatusDate] = useState('');
  const [addingStatusFishId, setAddingStatusFishId] = useState<number | null>(null);
  const [editingStatusId, setEditingStatusId] = useState<number | null>(null);
  const [editingStatusText, setEditingStatusText] = useState('');
  const [editingStatusDate, setEditingStatusDate] = useState('');
  const [fishStatuses, setFishStatuses] = useState<FishStatus[]>([]);
  const [expandedFish, setExpandedFish] = useState<{ [key: number]: boolean }>({});
  const [environmentData, setEnvironmentData] = useState<EnvironmentData[]>([]);
  const [isAddingEnvironment, setIsAddingEnvironment] = useState(false);
  const [newTemperature, setNewTemperature] = useState('');
  const [newPh, setNewPh] = useState('');
  const [newEnvironmentDate, setNewEnvironmentDate] = useState('');
  const [editingEnvironmentId, setEditingEnvironmentId] = useState<number | null>(null);
  const [editingTemperature, setEditingTemperature] = useState('');
  const [editingPh, setEditingPh] = useState('');
  const [editingEnvironmentDate, setEditingEnvironmentDate] = useState('');

  // 어항 단건 조회
  useEffect(() => {
    if (!isAuthenticated) return;
    
    fetch(`${baseUrl}/api/aquarium/${aquariumId}`, {
      credentials: 'include',
    })
      .then(res => res.json())
      .then(json => setAquarium(json.data))
      .catch(err => {
        console.error(err);
        // 인증 오류인 경우 로그인 페이지로 리다이렉트
        if (err.message && (err.message.includes('CMN006') || err.message.includes('인증이 필요합니다'))) {
          window.location.href = '/login';
        }
      });
  }, [aquariumId, isAuthenticated]);

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
  }, [isAuthenticated]);

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

  // 어항 로그 데이터 조회
  useEffect(() => {
    if (!isAuthenticated) return;
    
    fetch(`${baseUrl}/api/aquarium/${aquariumId}/aquariumLog`, {
      credentials: 'include',
    })
      .then(res => res.json())
      .then(json => {
        // ApiResponse 구조에서 data 추출
        const data = json?.data || [];
        // 날짜 최신순으로 정렬
        const sortedData = data.sort((a: EnvironmentData, b: EnvironmentData) => 
          new Date(b.logDate).getTime() - new Date(a.logDate).getTime()
        );
        setEnvironmentData(sortedData);
      })
      .catch(err => {
        console.error('Environment data fetch error:', err);
        // 인증 오류인 경우 로그인 페이지로 리다이렉트
        if (err.message && (err.message.includes('CMN006') || err.message.includes('인증이 필요합니다'))) {
          window.location.href = '/login';
        }
      });
  }, [aquariumId, isAuthenticated]);

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

  // 새 물고기 추가
  const handleAddAquarium = () => {
    setIsAdding(true);
  };

  const handleCancel = () => {
    setIsAdding(false);
    setNewFishName('');
    setNewFishSpecies('');
  };

  const handleConfirm = () => {
    if (newFishName.trim() === '') {
      alert('물고기 이름을 입력해주세요 :)');
      return;
    }
    if (newFishSpecies.trim() === '') {
        alert('물고기 종을 입력해주세요 :)');
        return;
    }

    fetch(`${baseUrl}/api/aquarium/${aquariumId}/fish`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ species: newFishSpecies, name: newFishName }),
    })
      .then(res => res.json())
      .then(json => {
        setFishes(prev => ([json.data, ...prev]));
        setNewFishName('');
        setNewFishSpecies('');
        setIsAdding(false);
      })
      .catch(err => {
        console.error(err);
        alert('물고기 추가 중 오류가 발생했습니다.');
      });
  };

  // 물고기 상태 추가
  const handleConfirmStatus = (fishId: number) => {
    if (newFishStatus.trim() === '') {
      alert('물고기 상태를 입력해주세요 :)');
      return;
    }
    if (newFishStatusDate.trim() === '') {
      alert('날짜를 입력해주세요 :)');
      return;
    }

    // 사용자가 입력한 날짜를 LocalDateTime 형식으로 변환
    const logDate = newFishStatusDate + 'T00:00:00'; // "2025-01-15T00:00:00" 형식

    fetch(`${baseUrl}/api/fish/${fishId}/fishLog`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        status: newFishStatus,
        logDate: logDate
      }),
    })
      .then(res => res.json())
      .then(json => {
        setFishStatuses(prev => [json.data, ...prev]);
        setNewFishStatus('');
        setNewFishStatusDate('');
        setAddingStatusFishId(null);
      })
      .catch(err => {
        console.error(err);
        alert('물고기 상태 추가 중 오류가 발생했습니다.');
      });
  };

  // 물고기 상태 삭제
  const handleDeleteStatus = (logId: number, fishId: number) => {
    if (window.confirm('정말로 이 상태를 삭제하시겠습니까?')) {
      fetch(`${baseUrl}/api/fish/${fishId}/fishLog/${logId}`, {
        method: 'DELETE',
        credentials: 'include',
      })
        .then(res => {
          if (res.ok) {
            setFishStatuses(prev => prev.filter(status => status.logId !== logId));
            alert('물고기 상태가 삭제되었습니다.');
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

  // 물고기 상태 수정
  const handleEditStatus = (logId: number, currentStatus: string, currentDate: string) => {
    setEditingStatusId(logId);
    setEditingStatusText(currentStatus);
    // 날짜를 YYYY-MM-DD 형식으로 변환
    const formattedDate = new Date(currentDate).toISOString().split('T')[0];
    setEditingStatusDate(formattedDate);
  };

  const handleCancelEdit = () => {
    setEditingStatusId(null);
    setEditingStatusText('');
    setEditingStatusDate('');
  };

  const handleConfirmEdit = (logId: number, fishId: number) => {
    if (editingStatusText.trim() === '') {
      alert('물고기 상태를 입력해주세요 :)');
      return;
    }
    if (editingStatusDate.trim() === '') {
      alert('날짜를 입력해주세요 :)');
      return;
    }

    // 사용자가 입력한 날짜를 LocalDateTime 형식으로 변환
    const logDate = editingStatusDate + 'T00:00:00';

    fetch(`${baseUrl}/api/fish/${fishId}/fishLog/${logId}`, {
      method: 'PUT',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        status: editingStatusText,
        logDate: logDate
      }),
    })
      .then(res => res.json())
      .then(json => {
        setFishStatuses(prev => 
          prev.map(status => 
            status.logId === logId 
              ? { ...status, status: editingStatusText, logDate: logDate }
              : status
          ).sort((a, b) => new Date(b.logDate).getTime() - new Date(a.logDate).getTime())
        );
        setEditingStatusText('');
        setEditingStatusDate('');
        setEditingStatusId(null);
      })
      .catch(err => {
        console.error(err);
        alert('물고기 상태 수정 중 오류가 발생했습니다.');
      });
  };

  // 어항 로그 데이터 추가
  const handleAddEnvironment = () => {
    setIsAddingEnvironment(true);
  };

  const handleCancelEnvironment = () => {
    setIsAddingEnvironment(false);
    setNewTemperature('');
    setNewPh('');
    setNewEnvironmentDate('');
  };

  const handleConfirmEnvironment = () => {
    if (newTemperature.trim() === '') {
      alert('온도를 입력해주세요 :)');
      return;
    }
    if (newPh.trim() === '') {
      alert('PH를 입력해주세요 :)');
      return;
    }
    if (newEnvironmentDate.trim() === '') {
      alert('날짜를 입력해주세요 :)');
      return;
    }

    const logDate = newEnvironmentDate + 'T00:00:00';

    const requestData: AquariumLogRequest = {
      aquariumId: parseInt(aquariumId),
      temperature: parseFloat(newTemperature),
      ph: parseFloat(newPh),
      logDate: logDate
    };

    fetch(`${baseUrl}/api/aquarium/${aquariumId}/aquariumLog`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestData),
    })
      .then(res => res.json())
      .then(json => {
        setEnvironmentData(prev => {
          const updated = [json.data, ...prev];
          return updated.sort((a, b) => new Date(b.logDate).getTime() - new Date(a.logDate).getTime());
        });
        setNewTemperature('');
        setNewPh('');
        setNewEnvironmentDate('');
        setIsAddingEnvironment(false);
      })
      .catch(err => {
        console.error(err);
        alert('환경 데이터 추가 중 오류가 발생했습니다.');
      });
  };

  // 어항 로그 데이터 수정
  const handleEditEnvironment = (logId: number, currentTemperature: number, currentPh: number, currentDate: string) => {
    setEditingEnvironmentId(logId);
    setEditingTemperature(currentTemperature.toString());
    setEditingPh(currentPh.toString());
    // 날짜를 YYYY-MM-DD 형식으로 변환
    const formattedDate = new Date(currentDate).toISOString().split('T')[0];
    setEditingEnvironmentDate(formattedDate);
  };

  const handleCancelEditEnvironment = () => {
    setEditingEnvironmentId(null);
    setEditingTemperature('');
    setEditingPh('');
    setEditingEnvironmentDate('');
  };

  const handleConfirmEditEnvironment = (logId: number) => {
    if (editingTemperature.trim() === '') {
      alert('온도를 입력해주세요 :)');
      return;
    }
    if (editingPh.trim() === '') {
      alert('PH를 입력해주세요 :)');
      return;
    }
    if (editingEnvironmentDate.trim() === '') {
      alert('날짜를 입력해주세요 :)');
      return;
    }

    const logDate = editingEnvironmentDate + 'T00:00:00';

    const requestData: AquariumLogRequest = {
      aquariumId: parseInt(aquariumId),
      temperature: parseFloat(editingTemperature),
      ph: parseFloat(editingPh),
      logDate: logDate
    };

    fetch(`${baseUrl}/api/aquarium/${aquariumId}/aquariumLog/${logId}`, {
      method: 'PUT',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestData),
    })
      .then(res => res.json())
      .then(json => {
        setEnvironmentData(prev => 
          prev.map(data => 
            data.logId === logId 
              ? { ...data, temperature: parseFloat(editingTemperature), ph: parseFloat(editingPh), logDate: logDate }
              : data
          ).sort((a, b) => new Date(b.logDate).getTime() - new Date(a.logDate).getTime())
        );
        setEditingTemperature('');
        setEditingPh('');
        setEditingEnvironmentDate('');
        setEditingEnvironmentId(null);
      })
      .catch(err => {
        console.error(err);
        alert('환경 데이터 수정 중 오류가 발생했습니다.');
      });
  };

  // 어항 로그 데이터 삭제
  const handleDeleteEnvironment = (logId: number) => {
    if (window.confirm('정말로 이 환경 데이터를 삭제하시겠습니까?')) {
      fetch(`${baseUrl}/api/aquarium/${aquariumId}/aquariumLog/${logId}`, {
        method: 'DELETE',
        credentials: 'include',
      })
        .then(res => {
          if (res.ok) {
            setEnvironmentData(prev => prev.filter(data => data.logId !== logId));
            alert('환경 데이터가 삭제되었습니다.');
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

  if (!aquarium) {
    return <div className="flex justify-center items-center h-screen">로딩 중...</div>;
  }

  return (
    <div className="min-h-screen bg-white p-6">
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* 헤더 */}
      <div className="flex items-center justify-center mb-8">
        <div className="flex items-center gap-2">
          <h1 className="text-2xl font-bold text-gray-800">🪸 {aquarium.aquariumName}</h1>
        </div>
      </div>

      {/* 내 물고기 섹션 */}
      <div className="mb-8">
        <div className="flex justify-between items-center mb-4">
          <button className="bg-blue-200 text-blue-800 px-4 py-2 rounded-lg font-medium">
            내 물고기
          </button>
          <button 
            onClick={handleAddAquarium}
            className="bg-purple-200 text-purple-800 px-4 py-2 rounded-lg font-medium"
          >
            + 물고기 추가
          </button>
        </div>

        {/* 물고기 추가를 위한 입력창 */}
        {isAdding && (
          <div className="mb-8 p-4 border border-gray-200 rounded-lg bg-gray-50">
            <label className="block text-gray-700 font-medium mb-2">
              새 물고기 이름 입력
            </label>
            <input
              type="text"
              value={newFishName}
              onChange={(e) => setNewFishName(e.target.value)}
              placeholder="예: 뚱이"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 mb-3 focus:outline-none focus:ring focus:ring-purple-200"
            />
            <label className="block text-gray-700 font-medium mb-2">
              새 물고기 종 입력
            </label>
            <input
              type="text"
              value={newFishSpecies}
              onChange={(e) => setNewFishSpecies(e.target.value)}
              placeholder="예: 금붕어"
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

        {/* 물고기 목록 */}
        <div className="space-y-4">
          {fishes && fishes.length > 0 ? (
            fishes.map((fish) => (
            <div key={fish.fishId} className="bg-gray-50 p-4 rounded-lg">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <span className="font-medium text-gray-800">{fish.fishName}</span>
                  <span className="bg-gray-200 text-gray-600 px-2 py-1 rounded-full text-sm">
                    {fish.fishSpecies}
                  </span>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => toggleFishStatus(fish.fishId)}
                    className="bg-blue-200 text-blue-800 px-3 py-1 rounded-lg text-sm"
                  >
                    {expandedFish[fish.fishId] ? '상태 닫기' : '상태 보기'}
                  </button>
                  <button
                    onClick={() => deleteFish(fish.fishId)}
                    className="bg-gray-200 text-gray-600 px-3 py-1 rounded-lg text-sm"
                  >
                    삭제
                  </button>
                </div>
              </div>
              <div className="mt-3 p-3">
                {expandedFish[fish.fishId] && (
                    <button 
                      onClick={() => setAddingStatusFishId(fish.fishId)}
                      className="bg-purple-200 text-purple-800 px-3 py-1 rounded-lg text-sm"
                    >
                      + 상태 추가
                    </button>
                )}
              </div>

              {/* 상태 추가 입력창 */}
              {addingStatusFishId === fish.fishId && (
                <div className="mt-3 p-3 border border-gray-200 rounded-lg bg-white">
                  <label className="block text-gray-700 font-medium mb-2">
                    물고기 상태 입력
                  </label>
                  <input
                    type="text"
                    value={newFishStatus}
                    onChange={(e) => setNewFishStatus(e.target.value)}
                    placeholder="예: 생생함, 아주 좋음, 보통"
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 mb-3 focus:outline-none focus:ring focus:ring-purple-200"
                  />
                  <label className="block text-gray-700 font-medium mb-2">
                    날짜 입력
                  </label>
                  <input
                    type="date"
                    value={newFishStatusDate}
                    onChange={(e) => setNewFishStatusDate(e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 mb-3 focus:outline-none focus:ring focus:ring-purple-200"
                  />
                  <div className="flex space-x-2">
                    <button
                      onClick={() => handleConfirmStatus(fish.fishId)}
                      className="bg-blue-100 hover:bg-blue-200 text-blue-800 px-4 py-2 rounded-lg text-sm transition-colors">
                      추가
                    </button>
                    <button
                      onClick={() => {
                        setAddingStatusFishId(null);
                        setNewFishStatus('');
                        setNewFishStatusDate('');
                      }}
                      className="bg-gray-100 hover:bg-gray-200 text-gray-800 px-4 py-2 rounded-lg text-sm transition-colors">
                      취소
                    </button>
                  </div>
                </div>
              )}

              {/* 상태 정보 (확장된 경우만 표시) */}
              {expandedFish[fish.fishId] && (
                <div className="mt-3 space-y-1">
                  {fishStatuses && fishStatuses.length > 0 ? (
                    fishStatuses
                      .filter(status => status && status.fishId === fish.fishId)
                      .map((status, index) => (
                        <div key={index} className="flex items-center justify-between text-sm text-gray-600">
                          <div className="flex items-center gap-3">
                            {editingStatusId === status.logId ? (
                              <>
                                <input
                                  type="date"
                                  value={editingStatusDate}
                                  onChange={(e) => setEditingStatusDate(e.target.value)}
                                  className="border border-gray-300 rounded px-2 py-1 text-sm"
                                />
                                <input
                                  type="text"
                                  value={editingStatusText}
                                  onChange={(e) => setEditingStatusText(e.target.value)}
                                  className="border border-gray-300 rounded px-2 py-1 text-sm"
                                />
                              </>
                            ) : (
                              <>
                                <span>{status.logDate ? new Date(status.logDate).toLocaleDateString('ko-KR') : ''}</span>
                                <span>{status.status || ''}</span>
                              </>
                            )}
                          </div>
                          <div className="flex gap-1">
                            {editingStatusId === status.logId ? (
                              <>
                                <button
                                  onClick={() => handleConfirmEdit(status.logId, fish.fishId)}
                                  className="bg-blue-100 hover:bg-blue-200 text-blue-800 px-2 py-1 rounded text-xs">
                                  확인
                                </button>
                                <button
                                  onClick={handleCancelEdit}
                                  className="bg-gray-100 hover:bg-gray-200 text-gray-800 px-2 py-1 rounded text-xs">
                                  취소
                                </button>
                              </>
                            ) : (
                              <>
                                <button
                                  onClick={() => handleEditStatus(status.logId, status.status, status.logDate)}
                                  className="bg-yellow-100 hover:bg-yellow-200 text-yellow-800 px-2 py-1 rounded text-xs">
                                  수정
                                </button>
                                <button
                                  onClick={() => handleDeleteStatus(status.logId, fish.fishId)}
                                  className="bg-red-100 hover:bg-red-200 text-red-800 px-2 py-1 rounded text-xs">
                                  삭제
                                </button>
                              </>
                            )}
                          </div>
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
      </div>

      {/* 온도 & PH 섹션 */}
      <div className="mb-8">
        <div className="flex justify-between items-center mb-4">
          <button className="bg-blue-200 text-blue-800 px-4 py-2 rounded-lg font-medium">
            온도 & PH
          </button>
          <button 
            onClick={handleAddEnvironment}
            className="bg-purple-200 text-purple-800 px-4 py-2 rounded-lg font-medium"
          >
            + 추가
          </button>
        </div>

        {/* 환경 데이터 추가를 위한 입력창 */}
        {isAddingEnvironment && (
          <div className="mb-8 p-4 border border-gray-200 rounded-lg bg-gray-50">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <div>
                <label className="block text-gray-700 font-medium mb-2">
                  온도 (°C)
                </label>
                <input
                  type="number"
                  step="0.1"
                  value={newTemperature}
                  onChange={(e) => setNewTemperature(e.target.value)}
                  placeholder="예: 24.5"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring focus:ring-purple-200"
                />
              </div>
              <div>
                <label className="block text-gray-700 font-medium mb-2">
                  PH
                </label>
                <input
                  type="number"
                  step="0.1"
                  value={newPh}
                  onChange={(e) => setNewPh(e.target.value)}
                  placeholder="예: 7.2"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring focus:ring-purple-200"
                />
              </div>
              <div>
                <label className="block text-gray-700 font-medium mb-2">
                  날짜
                </label>
                <input
                  type="date"
                  value={newEnvironmentDate}
                  onChange={(e) => setNewEnvironmentDate(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring focus:ring-purple-200"
                />
              </div>
            </div>
            <div className="flex space-x-2">
              <button
                onClick={handleConfirmEnvironment}
                className="bg-blue-100 hover:bg-blue-200 text-blue-800 px-4 py-2 rounded-lg text-sm transition-colors">
                추가
              </button>
              <button
                onClick={handleCancelEnvironment}
                className="bg-gray-100 hover:bg-gray-200 text-gray-800 px-4 py-2 rounded-lg text-sm transition-colors">
                취소
              </button>
            </div>
          </div>
        )}

        {/* 환경 데이터 목록 */}
        <div className="space-y-3">
          {environmentData && environmentData.length > 0 ? (
            environmentData.map((data, index) => (
              <div key={data.logId || index} className="bg-gray-50 p-4 rounded-lg">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-4 text-sm text-gray-600">
                    {editingEnvironmentId === data.logId ? (
                      <>
                        <input
                          type="date"
                          value={editingEnvironmentDate}
                          onChange={(e) => setEditingEnvironmentDate(e.target.value)}
                          className="border border-gray-300 rounded px-2 py-1 text-sm"
                        />
                        <input
                          type="number"
                          step="0.1"
                          value={editingTemperature}
                          onChange={(e) => setEditingTemperature(e.target.value)}
                          placeholder="온도"
                          className="border border-gray-300 rounded px-2 py-1 text-sm w-20"
                        />
                        <input
                          type="number"
                          step="0.1"
                          value={editingPh}
                          onChange={(e) => setEditingPh(e.target.value)}
                          placeholder="PH"
                          className="border border-gray-300 rounded px-2 py-1 text-sm w-20"
                        />
                      </>
                    ) : (
                      <>
                        <span className="font-medium">
                          {data.logDate ? new Date(data.logDate).toLocaleDateString('ko-KR') : ''}
                        </span>
                        <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-sm">
                          {data.temperature}°C
                        </span>
                        <span className="bg-green-100 text-green-800 px-2 py-1 rounded-full text-sm">
                          PH {data.ph}
                        </span>
                      </>
                    )}
                  </div>
                  <div className="flex gap-1">
                    {editingEnvironmentId === data.logId ? (
                      <>
                        <button
                          onClick={() => handleConfirmEditEnvironment(data.logId)}
                          className="bg-blue-100 hover:bg-blue-200 text-blue-800 px-2 py-1 rounded text-xs">
                          확인
                        </button>
                        <button
                          onClick={handleCancelEditEnvironment}
                          className="bg-gray-100 hover:bg-gray-200 text-gray-800 px-2 py-1 rounded text-xs">
                          취소
                        </button>
                      </>
                    ) : (
                      <>
                        <button
                          onClick={() => handleEditEnvironment(data.logId, data.temperature, data.ph, data.logDate)}
                          className="bg-yellow-100 hover:bg-yellow-200 text-yellow-800 px-2 py-1 rounded text-xs">
                          수정
                        </button>
                        <button
                          onClick={() => handleDeleteEnvironment(data.logId)}
                          className="bg-red-100 hover:bg-red-200 text-red-800 px-2 py-1 rounded text-xs">
                          삭제
                        </button>
                      </>
                    )}
                  </div>
                </div>
              </div>
            ))
          ) : (
            <div className="text-sm text-gray-500 text-center py-4">
              등록된 데이터가 없습니다.
            </div>
          )}
        </div>
      </div>
      </div>
    </div>
  );
}