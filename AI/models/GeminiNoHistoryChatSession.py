from google.generativeai.generative_models import ChatSession
from google.generativeai.types import (
    content_types,
    generation_types,
    helper_types,
    safety_types,
)
from google.generativeai.generative_models import _USER_ROLE


class NoHistoryChatSession(ChatSession):
    def send_message(
        self,
        content: content_types.ContentType,
        *,
        generation_config: generation_types.GenerationConfigType = None,
        safety_settings: safety_types.SafetySettingOptions = None,
        stream: bool = False,
        tools: content_types.FunctionLibraryType | None = None,
        tool_config: content_types.ToolConfigType | None = None,
        request_options: helper_types.RequestOptionsType | None = None,
    ) -> generation_types.GenerateContentResponse:
        """
        기존 send_message와 동일하지만 history에 메시지를 저장하지 않는 버전
        """
        if request_options is None:
            request_options = {}

        if self.enable_automatic_function_calling and stream:
            raise NotImplementedError(
                "Unsupported configuration: The `google.generativeai` SDK currently does not support the combination of `stream=True` and `enable_automatic_function_calling=True`."
            )

        tools_lib = self.model._get_tools_lib(tools)

        content = content_types.to_content(content)

        if not content.role:
            content.role = _USER_ROLE  # _USER_ROLE 대신 직접 "user" 사용

        # 시스템 프롬프트만 포함된 초기 히스토리 사용
        history = self.history[:1]  # 첫 번째 메시지(시스템 프롬프트)만 유지
        history.append(content)

        generation_config = generation_types.to_generation_config_dict(
            generation_config
        )
        if generation_config.get("candidate_count", 1) > 1:
            raise ValueError(
                "Invalid configuration: The chat functionality does not support `candidate_count` greater than 1."
            )

        response = self.model.generate_content(
            contents=history,
            generation_config=generation_config,
            safety_settings=safety_settings,
            stream=stream,
            tools=tools_lib,
            tool_config=tool_config,
            request_options=request_options,
        )

        self._check_response(response=response, stream=stream)

        if self.enable_automatic_function_calling and tools_lib is not None:
            _, content, response = self._handle_afc(
                response=response,
                history=history,
                generation_config=generation_config,
                safety_settings=safety_settings,
                stream=stream,
                tools_lib=tools_lib,
                request_options=request_options,
            )

        # 마지막 메시지 정보는 여전히 저장
        self._last_sent = content
        self._last_received = response

        return response
