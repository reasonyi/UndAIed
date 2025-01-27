import { useRef } from "react";
import { atom, useRecoilState } from "recoil";
import { Link, useLocation } from "react-router-dom";
import ReactQuill from "react-quill";
import "react-quill/dist/quill.snow.css";
import bugReportImage from "../assets/board/bugReport.png";

import Banner from "./Board/components/Banner";
import Header from "../components/Header";

const editorState = atom({
  key: "editorContent",
  default: "",
});

const formats = [
  "header",
  "bold",
  "italic",
  "underline",
  "strike",
  "list",
  "bullet",
  "indent",
  "link",
  "image",
];

function BoardWrite() {
  const [content, setContent] = useRecoilState(editorState);
  const quillRef = useRef<ReactQuill>(null);
  const location = useLocation();
  const prevPath = location.state?.from || "/board";

  const modules = {
    toolbar: [
      [{ header: [1, 2, false] }],
      ["bold", "italic", "underline", "strike"],
      ["link", "image"],
      [{ list: "ordered" }, { list: "bullet" }],
      ["clean"],
    ],
  };

  const handleSubmit = async () => {
    try {
      const postData = { content };
      const token = localStorage.getItem("token");

      console.log("Posted:", postData);
    } catch (error) {
      console.error("Error:", error);
    }
  };

  return (
    <>
      <Header scrollRatio={100} />
      <div className="h-14"></div>
      <Banner bannerImage={bugReportImage} category="write" />
      <div className="max-w-4xl mx-auto p-4">
        <div className="mb-4 border-t-2 border-t-black ">
          <input
            type="text"
            placeholder="제목을 입력하세요"
            className="focus:outline-none w-full py-2 px-4 border border-gray-300"
          />
        </div>

        <div>
          <ReactQuill
            placeholder="내용을 입력하세요"
            ref={quillRef}
            theme="snow"
            value={content}
            onChange={setContent}
            modules={modules}
            formats={formats}
            className="h-96 mb-12 border-t-black border-t-4"
          />
        </div>

        <div className="flex justify-center pt-4">
          <button
            onClick={handleSubmit}
            className="mx-1.5 px-9 py-2 bg-[#000000c0] text-white hover:bg-black border border-black"
          >
            확인
          </button>
          <Link
            to={prevPath}
            className="mx-1.5 px-9 py-2 bg-white border border-transparent text-black hover:border hover:border-black"
          >
            취소
          </Link>
        </div>
      </div>
    </>
  );
}

export default BoardWrite;
