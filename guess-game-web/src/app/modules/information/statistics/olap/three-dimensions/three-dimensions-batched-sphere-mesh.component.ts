import { Component, Input, Optional, SkipSelf } from '@angular/core';
import * as THREE from 'three';

// import { AbstractMesh, AbstractObject3D, RendererService } from 'atft';
import { AbstractMesh, AbstractObject3D } from '../../../../atft/object';
import { RendererService } from '../../../../atft/renderer';

import {
  ThreeDimensionsSphereGroupItem
} from '../../../../../shared/models/statistics/olap/three-dimensions/sphere/three-dimensions-sphere-group-item.model';

@Component({
  selector: 'app-three-dimensions-batched-sphere-mesh',
  template: '<ng-content></ng-content>',
  standalone: false
})
export class ThreeDimensionsBatchedSphereMeshComponent extends AbstractMesh {
  public readonly VERTEX_COUNT_FACTOR = 2048;
  public readonly INDEX_COUNT_FACTOR = 2048;

  @Input() widthSegments!: number;
  @Input() heightSegments!: number;
  @Input() sphereGroupItems!: ThreeDimensionsSphereGroupItem[];

  constructor(protected override rendererService: RendererService,
              @SkipSelf() @Optional() protected override parent: AbstractObject3D<any>) {
    super(rendererService, parent);
  }

  private fillMatrix(matrix: THREE.Matrix4, sphereGroupItem: ThreeDimensionsSphereGroupItem): THREE.Matrix4 {
    const position = new THREE.Vector3();
    const quaternion = new THREE.Quaternion(0, 0, 0);
    const scale = new THREE.Vector3(1, 1, 1);

    position.x = sphereGroupItem.translateX;
    position.y = sphereGroupItem.translateY;
    position.z = sphereGroupItem.translateZ;

    return matrix.compose(position, quaternion, scale);
  }

  protected newObject3DInstance(): THREE.Mesh {
    if (this.sphereGroupItems && (this.sphereGroupItems.length > 0)) {
      const material = this.getMaterial();
      const sphereGroupItemCount = this.sphereGroupItems.length;
      const geometryCount = sphereGroupItemCount;
      const vertexCount = sphereGroupItemCount * this.VERTEX_COUNT_FACTOR;
      const indexCount = sphereGroupItemCount * this.INDEX_COUNT_FACTOR;
      const batchedMesh = new THREE.BatchedMesh(geometryCount, vertexCount, indexCount, material);
      const matrix = new THREE.Matrix4();
      const geometryMap: Map<number, THREE.SphereGeometry> = new Map<number, THREE.SphereGeometry>();

      for (const sphereGroupItem of this.sphereGroupItems) {
        let geometry = geometryMap.get(sphereGroupItem.radius);

        if (!geometry) {
          geometry = new THREE.SphereGeometry(sphereGroupItem.radius, this.widthSegments, this.heightSegments);
          geometryMap.set(sphereGroupItem.radius, geometry);
        }

        // Add geometry into the batched mesh
        const geometryId = batchedMesh.addGeometry(geometry);

        // Create instance of this geometry
        const instanceId = batchedMesh.addInstance(geometryId);

        // Position the geometry
        batchedMesh.setMatrixAt(instanceId, this.fillMatrix(matrix, sphereGroupItem));
      }

      this.applyShadowProps(batchedMesh);

      return batchedMesh;
    } else {
      return new THREE.Mesh();
    }
  }
}
