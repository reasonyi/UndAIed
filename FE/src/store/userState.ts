import { atom } from 'recoil';
import { IUser } from '../types/User';

export const userState = atom<IUser>({
    key: 'userState',
    default: {
        id: null,
        username: '',
        token: 'default_token',
    },
})