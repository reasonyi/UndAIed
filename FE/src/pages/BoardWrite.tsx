import {useForm} from "react-hook-form";
import { useRecoilValue } from "recoil";
import { userState } from "../store/userState";

interface IForm {
    title: string;
    contents: string;
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