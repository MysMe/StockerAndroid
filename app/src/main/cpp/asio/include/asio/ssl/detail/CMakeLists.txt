cmake_minimum_required(VERSION 3.1)

#Automatically generated from files in this directory.
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/buffered_handshake_op.hpp")
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/engine.hpp")
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/handshake_op.hpp")
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/io.hpp")
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/openssl_init.hpp")
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/openssl_types.hpp")
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/password_callback.hpp")
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/read_op.hpp")
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/shutdown_op.hpp")
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/stream_core.hpp")
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/verify_callback.hpp")
target_sources(${PROJECT_NAME} PUBLIC "${CMAKE_CURRENT_LIST_DIR}/write_op.hpp")

#Automatically generated from subdirectories in this directory.
add_subdirectory("impl")
