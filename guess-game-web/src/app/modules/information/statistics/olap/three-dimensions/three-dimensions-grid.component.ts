import { Component, Input, OnChanges, Optional, SimpleChanges, SkipSelf } from '@angular/core';

// import { AbstractObject3D, RendererService } from 'atft';
import { AbstractObject3D } from '../../../../atft/object';
import { RendererService } from '../../../../atft/renderer';

import * as THREE from 'three';
import { BufferGeometry } from 'three';

@Component({
  selector: 'app-three-dimensions-grid',
  template: '<ng-content></ng-content>'
})
export class ThreeDimensionsGridComponent extends AbstractObject3D<THREE.LineSegments> implements OnChanges {
  @Input() sizeX = 100;
  @Input() divisionsX = 10;

  @Input() sizeY = 100;
  @Input() divisionsY = 10;

  @Input() sizeZ = 100;
  @Input() divisionsZ = 10;

  @Input() color: number | string = '#888888';

  constructor(protected override rendererService: RendererService,
              @SkipSelf() @Optional() protected override parent: AbstractObject3D<any>) {
    super(rendererService, parent);
  }

  protected getGeometry(): THREE.BufferGeometry {
    const sizeX = Math.abs(this.sizeX);
    const divisionsX = Math.abs(this.divisionsX);
    const sizeY = Math.abs(this.sizeY);
    const divisionsY = Math.abs(this.divisionsY);
    const sizeZ = Math.abs(this.sizeZ);
    const divisionsZ = Math.abs(this.divisionsZ);

    const color = new THREE.Color(this.color);

    const stepX = (divisionsX > 0) ? sizeX / divisionsX : 1;
    const stepY = (divisionsY > 0) ? sizeY / divisionsY : 1;
    const stepZ = (divisionsZ > 0) ? sizeZ / divisionsZ : 1;
    const halfSizeX = sizeX / 2;
    const halfSizeY = sizeY / 2;
    const halfSizeZ = sizeZ / 2;

    const vertices = [], colors: any[] = [];
    let colorIndex = 0;

    // X lines
    if (halfSizeX !== 0) {
      for (let y = -halfSizeY; y <= halfSizeY; y += stepY) {
        for (let z = -halfSizeZ; z <= halfSizeZ; z += stepZ) {
          vertices.push(-halfSizeX, y, z, halfSizeX, y, z);

          color.toArray(colors, colorIndex);
          colorIndex += 3;
          color.toArray(colors, colorIndex);
          colorIndex += 3;
        }
      }
    }

    // Y lines
    if (halfSizeY !== 0) {
      for (let x = -halfSizeX; x <= halfSizeX; x += stepX) {
        for (let z = -halfSizeZ; z <= halfSizeZ; z += stepZ) {
          vertices.push(x, -halfSizeY, z, x, halfSizeY, z);

          color.toArray(colors, colorIndex);
          colorIndex += 3;
          color.toArray(colors, colorIndex);
          colorIndex += 3;
        }
      }
    }

    // Z lines
    if (halfSizeZ !== 0) {
      for (let x = -halfSizeX; x <= halfSizeX; x += stepX) {
        for (let y = -halfSizeY; y <= halfSizeY; y += stepY) {
          vertices.push(x, y, -halfSizeZ, x, y, halfSizeZ);

          color.toArray(colors, colorIndex);
          colorIndex += 3;
          color.toArray(colors, colorIndex);
          colorIndex += 3;
        }
      }
    }

    const geometry = new BufferGeometry();
    geometry.setAttribute('position', new THREE.Float32BufferAttribute(vertices, 3));
    geometry.setAttribute('color', new THREE.Float32BufferAttribute(colors, 3));

    return geometry;
  }

  protected newObject3DInstance(): THREE.LineSegments {
    const geometry = this.getGeometry();
    const material = new THREE.LineBasicMaterial({vertexColors: true, toneMapped: false});

    return new THREE.LineSegments(geometry, material);
  }

  public override ngOnChanges(changes: SimpleChanges) {
    super.ngOnChanges(changes);

    if (!this.getObject()) {
      return;
    }

    let mustRerender = false;
    if (['sizeX', 'divisionsX', 'sizeY', 'divisionsY', 'sizeZ', 'divisionsZ', 'color'].some(propName => propName in changes)) {
      this.applyGeometry();
      mustRerender = true;
    }

    if (mustRerender) {
      this.rendererService.render();
    }
  }

  public applyGeometry() {
    this.getObject().geometry = this.getGeometry();
  }
}
