export interface SettingsStateType {
  isFullscreen: boolean;
  isMuted: boolean;
  volume: number;
}

export interface SettingProps {
  title: string;
  first: boolean;
  setFirst: (value: boolean) => void;
}
