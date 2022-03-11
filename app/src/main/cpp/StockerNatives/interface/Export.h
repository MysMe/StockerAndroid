#pragma once

#ifdef _WIN32
#define STOCKEREXPORT extern "C" __declspec(dllexport)
#else
#define STOCKEREXPORT
#endif