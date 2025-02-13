import {Component, Optional, SimpleChanges, SkipSelf} from '@angular/core';
import {RendererService} from '../../renderer/renderer.service';
import {ShaderPass} from 'three/examples/jsm/postprocessing/ShaderPass.js';
import {VerticalBlurShader} from 'three/examples/jsm/shaders/VerticalBlurShader.js';
import {HorizontalBlurShader} from 'three/examples/jsm/shaders/HorizontalBlurShader.js';
import {EffectComposerComponent} from './effect-composer.component';
import {AbstractComposeEffect} from './abstract-compose-effect';

@Component({
    selector: 'atft-blur',
    template: '',
    standalone: false
})
export class BlurComponent extends AbstractComposeEffect<ShaderPass> {

  constructor(
    protected override rendererService: RendererService,
    @SkipSelf() @Optional() protected override composer: EffectComposerComponent
  ) {
    super(rendererService, composer);
  }

  initPasses() {
    this.pass.push(new ShaderPass(VerticalBlurShader));
    this.pass.push(new ShaderPass(HorizontalBlurShader));
    this.pass.push(new ShaderPass(VerticalBlurShader));
    this.pass.push(new ShaderPass(HorizontalBlurShader));
  }

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  applyChanges(changes: SimpleChanges) {
    // TODO: implement changes
    return false;
  }

}
