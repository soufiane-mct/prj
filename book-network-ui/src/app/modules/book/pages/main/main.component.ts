import { Component } from '@angular/core';
import { MenuComponent } from "../../components/menu/menu.component";
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-main',
  imports: [MenuComponent,RouterModule],//hna drna import l MenuComponent bsh nkhdmo biha fl html d hd component maincomponent
  templateUrl: './main.component.html',
  styleUrl: './main.component.scss'
})
export class MainComponent {

}
