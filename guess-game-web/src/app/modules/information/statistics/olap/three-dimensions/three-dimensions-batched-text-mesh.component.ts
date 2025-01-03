import { Component, Input, Optional, SkipSelf } from '@angular/core';
import * as THREE from 'three';
import { Font } from 'three/examples/jsm/loaders/FontLoader.js';
import { TextGeometry } from 'three/examples/jsm/geometries/TextGeometry.js';

// import { AbstractLazyObject3D, AbstractObject3D, appliedMaterial, FontService, RendererService } from 'atft';
import { AbstractLazyObject3D, AbstractObject3D, FontService } from '../../../../atft/object';
import { RendererService } from '../../../../atft/renderer';
import { appliedMaterial } from '../../../../atft/util';

import {
  ThreeDimensionsTextGroup
} from '../../../../../shared/models/statistics/olap/three-dimensions/text/three-dimensions-text-group.model';
import {
  ThreeDimensionsTextGroupItem
} from '../../../../../shared/models/statistics/olap/three-dimensions/text/three-dimensions-text-group-item.model';
import {
  ThreeDimensionsTextAlignment
} from '../../../../../shared/models/statistics/olap/three-dimensions/text/three-dimensions-text-alignment.model';
import {
  ThreeDimensionsTextGeometryKey
} from '../../../../../shared/models/statistics/olap/three-dimensions/text/three-dimensions-text-geometry-key.model';

@Component({
  selector: 'app-three-dimensions-batched-text-mesh',
  template: '<ng-content></ng-content>'
})
export class ThreeDimensionsBatchedTextMeshComponent extends AbstractLazyObject3D {
  public readonly VERTEX_COUNT_FACTOR = 2048;
  public readonly INDEX_COUNT_FACTOR = 2048;

  @Input()
  material = 'basic';

  private _materialColor: string | number = '#DADADA';
  @Input()
  set materialColor(materialColor: string | number) {
    this._materialColor = materialColor;

    if (this.object) {
      this.startLoading();
    }
  }

  get materialColor() {
    return this._materialColor;
  }

  @Input()
  height = 0.3;

  @Input()
  curveSegments = 2;

  @Input()
  bevelEnabled = false;

  @Input()
  bevelThickness = 0.1;

  @Input()
  bevelSize = 0.1;

  @Input()
  bevelOffset = 0;

  @Input()
  bevelSegments = 1;

  @Input()
  fontUrl = './assets/fonts/inter_regular.typeface.json';

  @Input()
  castShadow = true;

  @Input()
  receiveShadow = true;

  @Input()
  depthWrite = true;

  private _textGroups: ThreeDimensionsTextGroup[] = [];
  @Input()
  set textGroups(textGroups: ThreeDimensionsTextGroup[]) {
    this._textGroups = textGroups;

    if (this.object) {
      this.startLoading();
    }
  }

  get textGroups() {
    return this._textGroups;
  }

  constructor(protected override rendererService: RendererService,
              @SkipSelf() @Optional() protected override parent: AbstractObject3D<any>, protected font: FontService) {
    super(rendererService, parent);
  }

  public getMaterial(): THREE.Material {
    return appliedMaterial(this.materialColor, this.material, this.depthWrite);
  }

  protected async loadLazyObject(): Promise<THREE.Object3D> {
    const font = await this.font.load(this.fontUrl);

    return this.getBatchedTextMesh(font);
  }

  getTranslateA(boxMinA: number, boxMaxA: number, alignA: ThreeDimensionsTextAlignment): number {
    if (alignA === ThreeDimensionsTextAlignment.Center) {
      return -((boxMaxA - boxMinA) / 2) - boxMinA;
    } else if (alignA === ThreeDimensionsTextAlignment.Left) {
      return -(boxMaxA - boxMinA);
    } else {
      return 0;
    }
  }

  private getMatrix(box: THREE.Box3, textGroupItem: ThreeDimensionsTextGroupItem): THREE.Matrix4 {
    // Initial translation by align
    const translateX = this.getTranslateA(box.min.x, box.max.x, textGroupItem.alignX);
    const translateY = this.getTranslateA(box.min.y, box.max.y, textGroupItem.alignY);
    const translateZ = this.getTranslateA(box.min.z, box.max.z, textGroupItem.alignZ);
    const initialTranslationM4 = new THREE.Matrix4().makeTranslation(translateX, translateY, translateZ);

    // Rotation
    const rotation = new THREE.Euler(textGroupItem.rotateX, textGroupItem.rotateY, textGroupItem.rotateZ);
    const rotationM4 = new THREE.Matrix4().makeRotationFromEuler(rotation);

    // Final translation
    const finalTranslation = new THREE.Matrix4().makeTranslation(textGroupItem.translateX, textGroupItem.translateY, textGroupItem.translateZ);

    const result = new THREE.Matrix4().multiplyMatrices(finalTranslation, rotationM4);
    result.multiply(initialTranslationM4);

    return result;
  }

  private getTextGroupItemCount(textGroups: ThreeDimensionsTextGroup[]) {
    let count = 0;

    for (const textGroup of textGroups) {
      count += textGroup.items.length;
    }

    return count;
  }

  protected getBatchedTextMesh(font: Font): THREE.Mesh {
    if (this.textGroups && (this.textGroups.length > 0)) {
      const material = this.getMaterial();
      const textGroupItemCount = this.getTextGroupItemCount(this.textGroups);
      const geometryCount = textGroupItemCount;
      const vertexCount = textGroupItemCount * this.VERTEX_COUNT_FACTOR;
      const indexCount = textGroupItemCount * this.INDEX_COUNT_FACTOR;
      const batchedMesh = new THREE.BatchedMesh(geometryCount, vertexCount, indexCount, material);
      const geometryMap: Map<string, TextGeometry> = new Map<string, TextGeometry>();

      batchedMesh.castShadow = this.castShadow;
      batchedMesh.receiveShadow = this.receiveShadow;

      for (const textGroup of this.textGroups) {
        const geometryKeyObject = new ThreeDimensionsTextGeometryKey(textGroup.text, textGroup.size);
        const geometryKey = JSON.stringify(geometryKeyObject);
        let geometry = geometryMap.get(geometryKey);

        if (!geometry) {
          geometry = new TextGeometry(textGroup.text, {
            font: font,
            size: textGroup.size,
            height: this.height,
            curveSegments: this.curveSegments,
            bevelEnabled: this.bevelEnabled,
            bevelThickness: this.bevelThickness,
            bevelSize: this.bevelSize,
            bevelOffset: this.bevelOffset,
            bevelSegments: this.bevelOffset
          });

          geometryMap.set(geometryKey, geometry);
        }

        const mesh = new THREE.Mesh(geometry, material);
        const box = new THREE.Box3().setFromObject(mesh);

        for (const textGroupItem of textGroup.items) {
          const id = batchedMesh.addGeometry(geometry);

          batchedMesh.setMatrixAt(id, this.getMatrix(box, textGroupItem));
        }
      }

      return batchedMesh;
    } else {
      return new THREE.Mesh();
    }
  }
}
