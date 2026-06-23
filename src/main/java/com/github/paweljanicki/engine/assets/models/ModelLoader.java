package com.github.paweljanicki.engine.assets.models;

import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.assimp.AIVector3D.Buffer;
import org.lwjgl.system.MemoryStack;

import com.github.paweljanicki.engine.assets.AssetManager;
import com.github.paweljanicki.engine.assets.textures.TextureParameters;

public class ModelLoader {
	
	public static Model load(AssetManager assetManager, String filePath, String texturesDirectory) {
		AIScene scene = Assimp.aiImportFile("src/main/resources" + filePath, Assimp.aiProcess_Triangulate | Assimp.aiProcess_FlipUVs | Assimp.aiProcess_CalcTangentSpace | Assimp.aiProcess_GenSmoothNormals);
		if (scene == null)
			throw new RuntimeException("[ERROR] >> Failed to load model '" + filePath + "'! Failure reason: " + Assimp.aiGetErrorString());
		
		try {
			List<Material> materials = new ArrayList<>();
			
			PointerBuffer materialsBuffer = scene.mMaterials();
			for (int i = 0; i < materialsBuffer.limit(); i++) {
				AIMaterial material = AIMaterial.create(materialsBuffer.get(i));
				materials.add(processMaterial(assetManager, texturesDirectory, material));
			}
			
			List<ModelPart> modelParts = new ArrayList<>();
			
			AINode rootNode = scene.mRootNode();
			processNode(scene, rootNode, new Matrix4f(), modelParts, materials);
			
			return new Model(modelParts);
		} finally {
			Assimp.aiReleaseImport(scene);
		}
	}
	
	private static Material processMaterial(AssetManager assetManager, String texturesDirectory, AIMaterial aiMaterial) {
		Material material = new Material();
		
		try (MemoryStack stack = MemoryStack.stackPush()) {
			AIColor4D albedo = AIColor4D.malloc(stack);
			float[] roughness = new float[1];
			float[] metallic = new float[1];
			
			AIString albedoMapPath = AIString.malloc(stack);
			AIString roughnessMapPath = AIString.malloc(stack);
			AIString metallicMapPath = AIString.malloc(stack);
			AIString aoMapPath = AIString.malloc(stack);
			AIString emissiveMapPath = AIString.malloc(stack);
			
			if (Assimp.aiGetMaterialColor(aiMaterial, Assimp.AI_MATKEY_COLOR_DIFFUSE, Assimp.aiTextureType_NONE, 0, albedo) == 0)
				material.setAlbedo(new Vector3f(albedo.r(), albedo.g(), albedo.b()));
			
			Assimp.aiGetMaterialFloatArray(aiMaterial, Assimp.AI_MATKEY_ROUGHNESS_FACTOR, Assimp.aiTextureType_NONE, 0, roughness, new int[] {1});
			material.setRoughness(roughness[0]);
			
			Assimp.aiGetMaterialFloatArray(aiMaterial, Assimp.AI_MATKEY_METALLIC_FACTOR, Assimp.aiTextureType_NONE, 0, metallic, new int[] {1});
			material.setMetallic(metallic[0]);
			
			if (Assimp.aiGetMaterialTexture(aiMaterial, Assimp.aiTextureType_DIFFUSE, 0, albedoMapPath, (IntBuffer) null, null, null, null, null, null) == 0)
				material.setAlbedoMap(assetManager.loadTexture(texturesDirectory + "/" + Paths.get(albedoMapPath.dataString()).getFileName().toString(), TextureParameters.DEFAULT_SRGBA));
			
			if (Assimp.aiGetMaterialTexture(aiMaterial, Assimp.aiTextureType_DIFFUSE_ROUGHNESS, 0, roughnessMapPath, (IntBuffer) null, null, null, null, null, null) == 0)
				material.setRoughnessMap(assetManager.loadTexture(texturesDirectory + "/" + Paths.get(roughnessMapPath.dataString()).getFileName().toString(), TextureParameters.DEFAULT_RGBA));
			
			if (Assimp.aiGetMaterialTexture(aiMaterial, Assimp.aiTextureType_METALNESS, 0, metallicMapPath, (IntBuffer) null, null, null, null, null, null) == 0)
				material.setMetallicMap(assetManager.loadTexture(texturesDirectory + "/" + Paths.get(metallicMapPath.dataString()).getFileName().toString(), TextureParameters.DEFAULT_RGBA));
			
			if (Assimp.aiGetMaterialTexture(aiMaterial, Assimp.aiTextureType_LIGHTMAP, 0, aoMapPath, (IntBuffer) null, null, null, null, null, null) == 0)
				material.setAoMap(assetManager.loadTexture(texturesDirectory + "/" + Paths.get(aoMapPath.dataString()).getFileName().toString(), TextureParameters.DEFAULT_RGBA));
			
			if (Assimp.aiGetMaterialTexture(aiMaterial, Assimp.aiTextureType_EMISSIVE, 0, emissiveMapPath, (IntBuffer) null, null, null, null, null, null) == 0)
				material.setEmissiveMap(assetManager.loadTexture(texturesDirectory + "/" + Paths.get(emissiveMapPath.dataString()).getFileName().toString(), TextureParameters.DEFAULT_SRGBA));
		}
		
		return material;
	}
	
	private static void processNode(AIScene scene, AINode node, Matrix4f parentTransformationMatrix, List<ModelPart> modelParts, List<Material> materials) {
		Matrix4f localTransformationMatrix = convertAIMatrix(node.mTransformation());
		Matrix4f globalTransformationMatrix = new Matrix4f(parentTransformationMatrix).mul(localTransformationMatrix);
		Matrix3f normalMatrix = new Matrix3f(globalTransformationMatrix).invert().transpose();
		
		for (int i = 0; i < node.mNumMeshes(); i++) {
			AIMesh mesh = AIMesh.create(scene.mMeshes().get(node.mMeshes().get(i)));
			modelParts.add(new ModelPart(processMesh(mesh, globalTransformationMatrix, normalMatrix), materials.get(mesh.mMaterialIndex())));
		}
		
		for (int i = 0; i < node.mNumChildren(); i++) {
			processNode(scene, AINode.create(node.mChildren().get(i)), globalTransformationMatrix, modelParts, materials);
		}
	}
	
	private static Mesh processMesh(AIMesh mesh, Matrix4f parentTransformationMatrix, Matrix3f parentNormalMatrix) {
		Buffer verticesBuffer = mesh.mVertices();
		Buffer textureCoordsBuffer = mesh.mTextureCoords(0);
		Buffer normalsBuffer = mesh.mNormals();
		org.lwjgl.assimp.AIFace.Buffer facesBuffer = mesh.mFaces();
		
		float[] positions = new float[verticesBuffer.limit() * 3];
		float[] textureCoords = new float[textureCoordsBuffer.limit() * 2];
		float[] normals = new float[normalsBuffer.limit() * 3];
		int[] indices = new int[facesBuffer.limit() * 3];
		
		for (int i = 0; i < verticesBuffer.limit(); i++) {
			AIVector3D vertex = verticesBuffer.get(i);
			
			Vector4f v = new Vector4f(vertex.x(), vertex.y(), vertex.z(), 1);
			v = parentTransformationMatrix.transform(v);
			
			positions[i * 3] = v.x();
			positions[i * 3 + 1] = v.y();
			positions[i * 3 + 2] = v.z();
		}
		
		for (int i = 0; i < textureCoordsBuffer.limit(); i++) {
			AIVector3D uv = textureCoordsBuffer.get(i);
			
			textureCoords[i * 2] = uv.x();
			textureCoords[i * 2 + 1] = uv.y();
		}
		
		for (int i = 0; i < normalsBuffer.limit(); i++) {
			AIVector3D normal = normalsBuffer.get(i);
			
			Vector3f n = new Vector3f(normal.x(), normal.y(), normal.z());
			parentNormalMatrix.transform(n);
			
			normals[i * 3] = n.x();
			normals[i * 3 + 1] = n.y();
			normals[i * 3 + 2] = n.z();
		}
		
		for (int i = 0; i < facesBuffer.limit(); i++) {
			AIFace face = facesBuffer.get(i);
			
			IntBuffer faceIndices = face.mIndices();
			
			for (int j = 0; j < faceIndices.limit(); j++) {
				indices[i * 3 + j] = faceIndices.get(j);
			}
		}
		
		return MeshLoader.load(positions, textureCoords, normals, indices);
	}
	
	private static Matrix4f convertAIMatrix(AIMatrix4x4 AIMatrix) {
		return new Matrix4f(
				AIMatrix.a1(), AIMatrix.b1(), AIMatrix.c1(), AIMatrix.d1(),
				AIMatrix.a2(), AIMatrix.b2(), AIMatrix.c2(), AIMatrix.d2(),
				AIMatrix.a3(), AIMatrix.b3(), AIMatrix.c3(), AIMatrix.d3(),
				AIMatrix.a4(), AIMatrix.b4(), AIMatrix.c4(), AIMatrix.d4()
		);
	}
	
}
