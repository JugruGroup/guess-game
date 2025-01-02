import {NgModule} from '@angular/core';
import {AtftCameraModule} from './camera';
import {AtftObjectModule} from './object';
import {AtftControlModule} from './control';
import {AtftPipeModule} from './pipe';
import {AtftRendererModule} from './renderer';
import {AtftAnimationModule} from './animation';
import {AtftRaycasterModule} from './raycaster';
import {AtftStatsModule} from './stats';
import {AtftEffectModule} from './effect';

// NOTE: In case of "ERROR in Unexpected value 'undefined' exported by the module 'AtftModule" fix imports (do not import index.ts)

@NgModule({
  imports: [
    AtftCameraModule,
    AtftObjectModule,
    AtftControlModule,
    AtftPipeModule,
    AtftRendererModule,
    AtftAnimationModule,
    AtftRaycasterModule,
    AtftStatsModule,
    AtftEffectModule
  ],
  exports: [
    AtftCameraModule,
    AtftObjectModule,
    AtftControlModule,
    AtftPipeModule,
    AtftRendererModule,
    AtftAnimationModule,
    AtftRaycasterModule,
    AtftStatsModule,
    AtftEffectModule
  ]
})
export class AtftModule {
}

