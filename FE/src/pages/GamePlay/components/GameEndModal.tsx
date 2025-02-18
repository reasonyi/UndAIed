function GameEndModal() {
  return (
    <div className="fixed inset-0 z-50">
      {/* 배경 */}
      <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" />

      {/* Modal Content */}
      <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-md">
        <div className="bg-gray-950 border border-[#8a6845] rounded-lg shadow-xl py-6 px-8"></div>
      </div>
    </div>
  );
}

export default GameEndModal;
