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
import ReactQuill from "react-quill";
import "react-quill/dist/quill.snow.css";
import bugReportImage from "../assets/board/bugReport.png";

import Banner from "./Board/components/Banner";

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
      console.log("Posted:", postData);
    } catch (error) {
      console.error("Error:", error);
    }
  };

  return (
    <>
      <Banner bannerImage={bugReportImage} category="write" />
      <div className="max-w-4xl mx-auto p-4">
        <div>자유게시판</div>
        <div className="mb-4">
          <input
            type="text"
            placeholder="제목을 입력하세요"
            className="w-full p-2 border border-gray-300 rounded"
          />
        </div>

        <ReactQuill
          ref={quillRef}
          theme="snow"
          value={content}
          onChange={setContent}
          modules={modules}
          formats={formats}
          className="h-96 mb-12"
        />

        <button
          onClick={handleSubmit}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          작성완료
        </button>
      </div>
    </>
  );
}

export default BoardWrite;
