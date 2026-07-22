#!/usr/bin/env python3
"""
SpotLocal Python AI - Model Configuration & Metadata Specification
"""

MODEL_NAME = "Mobile-UNet-Stems-INT8"
DEFAULT_MODEL_PATH = "models/mobile_unet_stems_int8.onnx"
SAMPLE_RATE = 44100
STEREO_CHANNELS = 2
QUANTIZATION_TYPE = "INT8"

MODEL_SPEC = {
    "architecture": "Mobile-UNet-Lite",
    "quantization": QUANTIZATION_TYPE,
    "input_shape": [1, STEREO_CHANNELS, SAMPLE_RATE],
    "output_stems": ["vocals", "instrumental"],
    "target_runtime": "ONNXRuntime-Mobile-Android",
    "file_size_bytes": 3840000
}
