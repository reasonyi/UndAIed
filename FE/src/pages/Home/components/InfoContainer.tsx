interface IInfoContainer {
  title: string;
  context: string;
}
function InfoContainer({ title, context }: IInfoContainer) {
  return (
    <div className="border-2 border-[#555555] rounded-xl p-8 grow min-w-[360px] w-[60vw] max-w-[600px] min-h-[360px] h-full max-h-[60vw] flex flex-col gap-4">
      <h1 className="text-3xl font-extrabold text-white mb-4">{title}</h1>
      <h3 className="text-lg text-[#a1a1aa] font-normal mb-4">{context}</h3>
    </div>
  );
}

export default InfoContainer;
