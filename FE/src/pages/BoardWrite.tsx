// import {useForm} from "react-hook-form";
// import { useRecoilValue } from "recoil";
// import { userState } from "../store/userState";

// interface IForm {
//     title: string;
//     contents: string;
// }

// function BoardWrite(){
//     //user정보 가져오기
//     //register는 첫번째 인자를 자동으로 html의 name로 할당하고, 이를 트리거한다.
//     const {register, watch, handleSubmit, formState :{errors}} = useForm<IForm>();
//     console.log(watch())

//     const userInfo = useRecoilValue(userState)

//     const boardSubmit = (data:any) => {
//         console.log(data);
//     }

//     return (
//         <>
//             <h1>BoardWrite page</h1>
//             <h1>유저 토큰: {userInfo.token}</h1>
//             <form onSubmit={handleSubmit(boardSubmit)}>
//                 <input type="text"
//                     {...register("title",
//                         {
//                             required: '제목은 필수 입력 사항입니다.',
//                         }
//                     )}
//                     className="border-4"
//                 />
//                 <input type="text"
//                     {...register("contents",
//                         {
//                             required: '내용은 필수 입력 사항입니다.',
//                         }
//                     )}
//                     className="border-4"
//                 />
//                 <button
//                   type="submit"
//                   className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600 transition duration-200"
//                 >
//                   제출하기
//                 </button>
//             </form>
//             <p className="text-red-600">{errors.title?.message}</p>
//             <p className="text-red-600">{errors.contents?.message}</p>
//         </>
//     )
// }

// export default BoardWrite;
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
