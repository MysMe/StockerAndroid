cmake_minimum_required(VERSION 3.1)

#Automatically generated from files in this directory.
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/basic_endpoint.hpp")
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/connect_pair.hpp")
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/datagram_protocol.hpp")
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/stream_protocol.hpp")

#Automatically generated from subdirectories in this directory.
add_subdirectory("detail")
