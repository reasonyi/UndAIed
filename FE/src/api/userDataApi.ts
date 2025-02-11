import { useQuery } from "@tanstack/react-query";
import axios from "axios";

export const userApi = axios.create({
  baseURL: "https://i12b212.p.ssafy.io",
  // baseURL: "http://localhost5173.com",
});