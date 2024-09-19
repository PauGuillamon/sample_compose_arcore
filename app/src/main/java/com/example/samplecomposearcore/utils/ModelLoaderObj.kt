package com.example.samplecomposearcore.utils

import android.content.res.AssetManager
import com.example.samplecomposearcore.geometry.Mesh
import com.example.samplecomposearcore.geometry.Vertex
import com.example.samplecomposearcore.geometry.VertexIndex
import com.example.samplecomposearcore.math.Vector2
import com.example.samplecomposearcore.math.Vector3
import com.example.samplecomposearcore.opengl.Material
import com.example.samplecomposearcore.opengl.MaterialColored
import com.example.samplecomposearcore.opengl.MaterialTextured
import com.example.samplecomposearcore.opengl.RenderableMesh
import com.example.samplecomposearcore.opengl.RenderableModel
import de.javagl.obj.MtlReader
import de.javagl.obj.Obj
import de.javagl.obj.ObjData
import de.javagl.obj.ObjReader
import de.javagl.obj.ObjSplitting
import de.javagl.obj.ObjUtils
import java.io.File
import java.nio.FloatBuffer

private const val TAG = "ModelReaderObj"

fun loadModel(assetManager: AssetManager, filename: String): RenderableModel {
    val file = File(filename)
    Logger.LogInfo(TAG, "Loading model \"$filename\"")
    val folderPath = file.parent!!
    val objInputStream = assetManager.open(file.path)
    val renderableMeshes = mutableListOf<Pair<RenderableMesh, Material>>()

    val rawObj = ObjReader.read(objInputStream)
    val mtlfile = rawObj.mtlFileNames[0]
    val mapMaterials: Map<String, Material> = processMtlFile(assetManager, folderPath, mtlfile)
    ObjSplitting.splitByMaterialGroups(rawObj).forEach { it: Map.Entry<String, Obj> ->
        Logger.LogInfo(TAG, "\t\tFound mesh with material:\"${it.key}\"")
        val mtlName = it.key
        val currentMtlId = "$mtlfile/$mtlName"
        //val obj = ObjUtils.triangulate()
        val obj = ObjUtils.convertToRenderable(it.value)
        if (mapMaterials.contains(currentMtlId)) {
            val mesh = generateMeshFromObj(obj)
            renderableMeshes.add(
                Pair(
                    RenderableMesh().apply { setMesh(mesh) },
                    mapMaterials[currentMtlId]!!
                )
            )
        }
    }
    objInputStream.close()

    return RenderableModel(renderableMeshes)
}

private fun processMtlFile(
    assetManager: AssetManager,
    folderPath: String,
    mtlFilename: String
): Map<String, Material> {
    val map = mutableMapOf<String, Material>()
    val mtlInputStream = assetManager.open("$folderPath/$mtlFilename")
    val materials = MtlReader.read(mtlInputStream)
    materials.forEach {
        val diffuseTextureFilename: String? = it.mapKd
        Logger.LogInfo(TAG, "\t\tMaterial \"${it.name}\" found with diffuseTextureFilename:\"$diffuseTextureFilename\"")
        val material = if (diffuseTextureFilename.isNullOrBlank()) {
            val diffuseColor = it.kd
            MaterialColored(assetManager, Vector3(
                diffuseColor.x,
                diffuseColor.y,
                diffuseColor.z
            ))
        } else {
            val diffuseTextureFullPath = "$folderPath/$diffuseTextureFilename"
            MaterialTextured(
                assetManager,
                diffuseTextureFullPath
            )
        }
        map["$mtlFilename/${it.name}"] = material
    }
    mtlInputStream.close()
    return map
}

fun generateMeshFromObj(obj: Obj): Mesh {
    val objVertices: FloatBuffer = ObjData.getVertices(obj)
    val objTexCoords = ObjData.getTexCoords(obj, 2)
    val objNormals = ObjData.getNormals(obj)
    val objIndices = ObjData.getFaceVertexIndices(obj)

    val numVertices = objVertices.capacity() / 3
    val vertices = ArrayList<Vertex>(numVertices)
    for (i in 0..< objVertices.capacity() step 3) {
        val pos = Vector3(
            objVertices.get(i),
            objVertices.get(i + 1),
            objVertices.get(i + 2)
        )
        val normal = Vector3(
            objNormals.get(i),
            objNormals.get(i + 1),
            objNormals.get(i + 2)
        ).normalized()
        val texCoordIndex = (i / 3) * 2
        val texCoords = Vector2(
            objTexCoords.get(texCoordIndex),
            objTexCoords.get(texCoordIndex + 1)
        )
        vertices.add(
            Vertex.Builder()
                .position(pos)
                .texCoords(texCoords)
                .normal(normal)
                .build()
        )
    }

    val indices = ArrayList<VertexIndex>(objIndices.capacity())
    // If Mesh accepted an IntBuffer directly, this conversion wouldn't be needed
    for (i in 0 ..< objIndices.capacity()) {
        indices.add(objIndices.get(i))
    }
    val mesh = Mesh()
    mesh.load(vertices, indices)
    return mesh
}
