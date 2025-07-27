import React, { useState, useEffect } from "react";
import L from "leaflet";

const App = () => {
  // 世界の主要都市の座標を配列で定義（日本含む）
  const cities = [
    { name: "大阪", lat: 34.0522, lon: 135.7558 },
    { name: "名古屋", lat: 35.1815, lon: 136.9066 },
    { name: "札幌", lat: 43.0621, lon: 141.3544 },
    { name: "福岡", lat: 33.5902, lon: 130.4017 },
    { name: "ニューヨーク", lat: 40.7128, lon: -74.0060 },
    { name: "ロンドン", lat: 51.5074, lon: -0.1278 },
    { name: "パリ", lat: 48.8566, lon: 2.3522 },
    { name: "ベルリン", lat: 52.5200, lon: 13.4050 },
    { name: "シドニー", lat: -33.8688, lon: 151.2093 },
    { name: "カイロ", lat: 30.0444, lon: 31.2357 },
    { name: "バンコク", lat: 13.7563, lon: 100.5018 },
    { name: "ロサンゼルス", lat: 34.0522, lon: -118.2437 },
    { name: "サンパウロ", lat: -23.5505, lon: -46.6333 }
  ];

  // 東京を目的地として固定
  const destination = { name: "東京", lat: 35.6762, lon: 139.6503 };

  // ランダムに初期地点を選ぶ（東京を除く）
  const getRandomCity = () => cities[Math.floor(Math.random() * cities.length)];

  // 状態管理
  const [randomCity, setRandomCity] = useState(getRandomCity());
  const [attempts, setAttempts] = useState(4); // 解答回数を4回に設定
  const [hintUsed, setHintUsed] = useState(0); // ヒント使用回数
  const [distance, setDistance] = useState(0);
  const [userAnswer, setUserAnswer] = useState("");
  const [hints, setHints] = useState([]); // ヒントを配列で保持
  const [map, setMap] = useState(null); // 地図のインスタンスを保持
  const [gameOver, setGameOver] = useState(false); // ゲームオーバーの状態

  // 初期地点と目的地の距離を計算（メートル単位）
  useEffect(() => {
    const initialLocation = L.latLng(randomCity.lat, randomCity.lon);
    const destinationLocation = L.latLng(destination.lat, destination.lon);
    const distanceValue = initialLocation.distanceTo(destinationLocation);
    setDistance(distanceValue);
    
    // 地図を作成
    if (map) {
      map.remove(); // 前の地図を削除
    }

    const newMap = L.map("map").setView([destination.lat, destination.lon], 5);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: "&copy; <a href='https://www.openstreetmap.org/copyright'>OpenStreetMap</a> contributors"
    }).addTo(newMap);
    
    // 初期地点と目的地のマーカーを追加
    L.marker([randomCity.lat, randomCity.lon]).addTo(newMap).bindPopup(randomCity.name).openPopup();
    L.marker([destination.lat, destination.lon]).addTo(newMap).bindPopup(destination.name).openPopup();
    
    // 初期地点と目的地を線で結ぶ
    const latlngs = [
      [randomCity.lat, randomCity.lon],
      [destination.lat, destination.lon]
    ];
    L.polyline(latlngs, { color: "blue" }).addTo(newMap);

    setMap(newMap); // 地図のインスタンスを更新
  }, [randomCity]);  // randomCityが変更されるたびに再実行

  const checkAnswer = () => {
    const userDistance = parseFloat(userAnswer);
    if (isNaN(userDistance)) {
      alert("正しい数字を入力してください。");
      return;
    }

    setAttempts((prev) => prev - 1);

    if (Math.abs(userDistance - distance) < 1000) {
      alert(`正解! 正解の距離: ${distance.toFixed(2)} メートル`);
      // 新しい都市をランダムに選び直す
      setRandomCity(getRandomCity());
    } else if (attempts <= 1) {
      alert(`失敗! 正解の距離: ${distance.toFixed(2)} メートル`);
      setGameOver(true); // ゲームオーバー
    } else {
      alert(`間違い。残り回数: ${attempts}`);
      if (hintUsed < 3) {
        giveHint(userDistance);
      }
    }
  };

  const giveHint = (userDistance) => {
    const japanLength = 3000000; // 日本の南北の長さ：約3000km (3000000m)
    let newHint = "";
    
    if (hintUsed === 0) {
      newHint = `ヒント: 日本の南北の長さは約3000km（3000000m）です。`;
    } else if (hintUsed === 1) {
      const diff = Math.abs(userDistance - distance);
      const numJapanLengths = (diff / japanLength).toFixed(2);
      newHint = `ヒント: あなたの予想との差は日本の長さの約${numJapanLengths}個分です。`;
    } else if (hintUsed === 2) {
      const diff = Math.abs(userDistance - distance);
      newHint = `ヒント: あなたの予想と正解の距離の差は約${diff.toFixed(2)} メートルです。`;
    }

    // 既存のヒントに新しいヒントを追加
    setHints((prevHints) => [...prevHints, newHint]);
    setHintUsed(hintUsed + 1);
  };

  const restartGame = () => {
    setRandomCity(getRandomCity());
    setAttempts(4); // 回数をリセット
    setHintUsed(0); // ヒント使用回数もリセット
    setUserAnswer(""); // ユーザーの回答もリセット
    setHints([]); // ヒントリストもリセット
    setGameOver(false); // ゲームオーバー状態をリセット
  };

  return (
    <div style={{ display: "flex", justifyContent: "space-between", padding: "10px" }}>
      <div id="info" style={{ width: "20%", padding: "10px", backgroundColor: "#f0f0f0", borderRadius: "8px", boxShadow: "0 2px 4px rgba(0, 0, 0, 0.1)" }}>
        <h2 style={{ textAlign: "center" }}>距離情報</h2>
        <p>初期地点: {randomCity.name} ({randomCity.lat.toFixed(4)}, {randomCity.lon.toFixed(4)})</p>
        <p>目的地: 東京</p>

        <div id="answerSection">
          <label>距離を予想してください（メートル）:</label>
          <input
            type="number"
            value={userAnswer}
            onChange={(e) => setUserAnswer(e.target.value)}
            disabled={gameOver} // ゲームオーバー時には入力不可にする
          />
          <button onClick={checkAnswer} disabled={gameOver || attempts <= 0}>答える</button>
          <p>残り回数: {attempts}</p>
          <div style={{ color: "blue" }}>
            {hints.map((hint, index) => (
              <p key={index}>{hint}</p>
            ))}
          </div>
        </div>

        <button onClick={restartGame} style={{ marginTop: "20px" }} disabled={!gameOver}>
          再スタート
        </button>
      </div>

      <div id="map" style={{ height: "500px", width: "80%" }}></div>
    </div>
  );
};

export default App;
