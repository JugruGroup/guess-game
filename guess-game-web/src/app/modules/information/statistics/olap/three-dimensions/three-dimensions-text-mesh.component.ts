import { Component, Input, Optional, SkipSelf } from '@angular/core';
import * as THREE from 'three';

// import { AbstractLazyObject3D, AbstractObject3D, appliedMaterial, FontService, RendererService } from 'atft';
import { AbstractLazyObject3D, AbstractObject3D, FontService } from '../../../../atft/object';
import { RendererService } from '../../../../atft/renderer';
import { appliedMaterial } from '../../../../atft/util';

import { Font } from 'three/examples/jsm/loaders/FontLoader';
import { TextGeometry } from 'three/examples/jsm/geometries/TextGeometry';
import {
  ThreeDimensionsTextAlignment
} from '../../../../../shared/models/statistics/olap/three-dimensions/text/three-dimensions-text-alignment.model';

@Component({
  selector: 'app-three-dimensions-text-mesh',
  template: '<ng-content></ng-content>'
})
export class ThreeDimensionsTextMeshComponent extends AbstractLazyObject3D {
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

  private _text = 'Text';
  @Input()
  set text(text: string) {
    this._text = text;

    if (this.object) {
      this.startLoading();
    }
  }

  get text() {
    return this._text;
  }

  @Input()
  size = 10;

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

  @Input()
  alignX = ThreeDimensionsTextAlignment.Center;

  @Input()
  alignY = ThreeDimensionsTextAlignment.Center;

  @Input()
  alignZ = ThreeDimensionsTextAlignment.Center;

  constructor(protected override rendererService: RendererService,
              @SkipSelf() @Optional() protected override parent: AbstractObject3D<any>, protected font: FontService) {
    super(rendererService, parent);
  }

  public getMaterial(): THREE.Material {
    return appliedMaterial(this.materialColor, this.material, this.depthWrite);
  }

  protected async loadLazyObject(): Promise<THREE.Object3D> {
    const font = await this.font.load(this.fontUrl);

    return this.getTextMesh(font);
  }

  protected getTextMesh(font: Font): THREE.Mesh {
    if (this.text) {
      const geometry = new TextGeometry(this.text, {
        font: font,
        size: this.size,
        height: this.height,
        curveSegments: this.curveSegments,
        bevelEnabled: this.bevelEnabled,
        bevelThickness: this.bevelThickness,
        bevelSize: this.bevelSize,
        bevelOffset: this.bevelOffset,
        bevelSegments: this.bevelOffset
      });

      const material = this.getMaterial();
      const mesh = new THREE.Mesh(geometry, material);
      mesh.castShadow = this.castShadow;
      mesh.receiveShadow = this.receiveShadow;

      const box = new THREE.Box3().setFromObject(mesh);

      if (this.alignX === ThreeDimensionsTextAlignment.Center) {
        mesh.translateX(-((box.max.x - box.min.x) / 2) - box.min.x);
      } else if (this.alignX === ThreeDimensionsTextAlignment.Left) {
        mesh.translateX(-(box.max.x - box.min.x));
      }

      if (this.alignY === ThreeDimensionsTextAlignment.Center) {
        mesh.translateY(-((box.max.y - box.min.y) / 2) - box.min.y);
      } else if (this.alignY === ThreeDimensionsTextAlignment.Left) {
        mesh.translateY(-(box.max.y - box.min.y));
      }

      if (this.alignZ === ThreeDimensionsTextAlignment.Center) {
        mesh.translateZ(-((box.max.z - box.min.z) / 2) - box.min.z);
      } else if (this.alignZ === ThreeDimensionsTextAlignment.Left) {
        mesh.translateZ(-(box.max.z - box.min.z));
      }

      return mesh;
    } else {
      return new THREE.Mesh();
    }
  }
}
