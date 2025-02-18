import 'vuetify/styles'
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import {mdi} from 'vuetify/iconsets/mdi'



export default createVuetify({
    //components는 vuetify에서 사용할 수 있는 UI컴포넌트들을 의미
    components,
    //directives는 
    directives,
    icons: {
        defaultSet: 'mdi',
        sets:{
            mdi
        }
    }
});