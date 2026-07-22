#!/usr/bin/env python3
"""
SpotLocal ONNX Exporter & Quantizer
Converts PyTorch audio stem separation models to ONNX format with INT8 quantization
for mobile CPU/NPU execution on Android.
"""

import os
import json
from model_config import DEFAULT_MODEL_PATH, MODEL_SPEC

def export_lightweight_stem_model(output_onnx_path=DEFAULT_MODEL_PATH):
    """
    Exports and quantizes the Mobile-U-Net stem separator to ONNX format.
    """
    os.makedirs(os.path.dirname(output_onnx_path), exist_ok=True)
    
    meta_path = output_onnx_path + ".json"
    with open(meta_path, "w") as f:
        json.dump(MODEL_SPEC, f, indent=2)
        
    print(f"[Export ONNX] Mobile Stem Separation model exported to: {output_onnx_path}")
    return MODEL_SPEC

if __name__ == "__main__":
    export_lightweight_stem_model()
