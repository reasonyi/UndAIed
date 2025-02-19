<<<<<<< HEAD
import {useForm} from "react-hook-form";
import { useRecoilValue } from "recoil";
import { userState } from "../store/userState";

interface IForm {
    title: string;
    contents: string;
=======
import { useRef } from "react";
import { atom, useRecoilState } from "recoil";
import { Link, useLocation, useNavigate } from "react-router-dom";
import ReactQuill from "react-quill";
import "react-quill/dist/quill.snow.css";
import { useForm, Controller, FieldValues } from "react-hook-form";
import bugReportImage from "../assets/board/bugReport.png";
import { boardApi } from "../api/boardApi";
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

interface FormInputs {
  title: string;
  content: string;
}

function BoardWrite() {
  const [content, setContent] = useRecoilState(editorState);
  const quillRef = useRef<ReactQuill>(null);
  const location = useLocation();
  const navigate = useNavigate();
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

  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<FormInputs>();

  // HTML 컨텐츠가 실제로 비어있는지 확인하는 함수
  const isEmptyHtml = (html: string) => {
    // HTML 태그를 제거하고 공백을 제거한 후 길이가 0인지 확인
    const text = html.replace(/<(.|\n)*?>/g, "").trim();
    return text.length === 0;
  };

  const onSubmit = async (data: any) => {
    try {
      // 내용이 비어있는지 확인
      if (isEmptyHtml(data.content)) {
        throw new Error("내용을 입력하세요.");
      }

      await boardApi.createPost(data);
      navigate(prevPath);
    } catch (error) {
      console.error("Error creating post:", error);
      // 에러 메시지를 사용자에게 표시하는 로직 추가 가능
    }
  };

  return (
    <>
      <Header scrollRatio={100} />
      <div className="h-14"></div>
      <Banner bannerImage={bugReportImage} category="write" />
      <div className="max-w-4xl mx-auto p-4">
        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="mb-4 border-t-2 border-t-black">
            <Controller
              name="title"
              control={control}
              defaultValue=""
              rules={{ required: "제목을 입력하세요." }}
              render={({ field }) => (
                <input
                  {...field}
                  type="text"
                  placeholder="제목을 입력하세요"
                  className="focus:outline-none w-full py-2 px-4 border border-gray-300"
                />
              )}
            />
            {errors.title && typeof errors.title.message === "string" && (
              <p className="text-red-500">{errors.title.message}</p>
            )}
          </div>
          <div>
            <Controller
              name="content"
              control={control}
              defaultValue=""
              rules={{
                required: "내용을 입력하세요.",
                validate: {
                  notEmpty: (value) =>
                    !isEmptyHtml(value) || "내용을 입력하세요.",
                },
              }}
              render={({ field }) => (
                <ReactQuill
                  {...field}
                  placeholder="내용을 입력하세요"
                  ref={quillRef}
                  theme="snow"
                  modules={modules}
                  formats={formats}
                  className="h-96 mb-12 border-t-black border-t-4"
                  onChange={(value) => {
                    field.onChange(value);
                    setContent(value);
                  }}
                />
              )}
            />
            {errors.content && typeof errors.content.message === "string" && (
              <p className="text-red-500">{errors.content.message}</p>
            )}
          </div>
          <div className="flex justify-center pt-4">
            <button
              type="submit"
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
        </form>
      </div>
    </>
  );
>>>>>>> release
}

function BoardWrite(){
    //user정보 가져오기
    //register는 첫번째 인자를 자동으로 html의 name로 할당하고, 이를 트리거한다.
    const {register, watch, handleSubmit, formState :{errors}} = useForm<IForm>();
    console.log(watch())

    const userInfo = useRecoilValue(userState)

    const boardSubmit = (data:any) => {
        console.log(data);
    }

    return (
        <>
            <h1>BoardWrite page</h1>
            <h1>유저 토큰: {userInfo.token}</h1>
            <form onSubmit={handleSubmit(boardSubmit)}>
                <input type="text"
                    {...register("title",
                        {
                            required: '제목은 필수 입력 사항입니다.',
                        }
                    )}
                    className="border-4"
                />
                <input type="text"
                    {...register("contents",
                        {
                            required: '내용은 필수 입력 사항입니다.',
                        }
                    )}
                    className="border-4"
                />
                <button
                  type="submit"
                  className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600 transition duration-200"
                >
                  제출하기
                </button>
            </form>
            <p className="text-red-600">{errors.title?.message}</p>
            <p className="text-red-600">{errors.contents?.message}</p>
        </>
    )
}

export default BoardWrite;