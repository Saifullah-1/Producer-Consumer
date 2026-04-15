<template>
        <div ref="tools" id="bar">
            <div class="tools" style="justify-content: left">
                <button class="button" @click="Add('machine')">Machine</button>
                <button class="button" @click="Add('queue')">Queue</button>
            </div>
            <div class="tools">
                <button class="button" @click="select" ref="mouse">Select</button>
                <button class="button" @click="Link" ref="link">Link</button>
                <button class="button" @click="Clear">Clear</button>
            </div>
            <div class="tools" style="justify-content: right">
                <input  type="number" min="1" value="10" ref="products">
                <button class="button" @click="run('run')" ref="run">Run</button>
                <button class="button" @click="run('replay')" ref="replay">Replay</button>
            </div>
        </div>
        <div ref="container" @click="coordinates" id="container"></div>
</template>

<script>
import axios from 'axios'
import Konva from 'konva'
import { isProxy, toRaw } from 'vue';


export default {
    data(){
        const currentHost = window.location.hostname
        const apiBaseUrl = currentHost.includes('app.github.dev')
            ? `${window.location.protocol}//${currentHost.replace('-8081.', '-8080.')}/`
            : 'http://localhost:8080/'

        return {
            isDrawing : false,
            arrow : null,
            arrowSource : [],
            arrowsList : [],
            machines:[],
            queues : [],
            Qindex : 0,
            Mindex : 0,
            Aindex : 0,
            url : apiBaseUrl,
            link : false,
            finished : true
        }
    },
    methods:{
        Add(type){
            this.noArrows()
            let item
            let Group = new Konva.Group({
                x : 550,
                y : 85,
                name : type,
                draggable : true,
            })
            let text = new Konva.Text({
                fontSize: 25,
                fontFamily: 'Calibri',
                width: 50,
                align: 'center'
            })
            
            if(type == 'machine'){     
                this.machines.push(Group)
                item = new Konva.Circle({
                    radius : 30,
                    fill : '#12cc7c',
                    id : ++this.Mindex,
                });
                text.x(item.x()-25).y(item.y()-10)
                text.text('M'.concat(item.id()))
            }
            else {
                Group.x(Group.x()-25).y(Group.y()-25)
                this.queues.push(Group)
                item = new Konva.Rect({
                    height : 50,
                    width : 50,
                    fill : 'yellow',
                    id : ++this.Qindex,
                    cornerRadius : 3,
                });
                text.x(item.x()).y(item.y()+4)
                text.text('Q'.concat(item.id())+'\n0')
            }
            item.stroke ('black').strokeWidth ('0.5').name (type)
            Group.id(item.id())
            Group.add(item).add(text)
            this.layer.add(Group).batchDraw();
            this.select()
            let machine = type=='machine'? true : false
            const response = axios.post(this.url+'addItem?isMachine='+machine)
        },
        noArrows(){
            if (this.isDrawing){
                this.layer.children= this.layer.children.slice(0,this.layer.children.length-1)
                this.isDrawing = false
            }
            this.$refs.mouse.style.background="yellow"
            this.$refs.link.style.background="#12cc7c"
        },
        select(){
            this.noArrows()
            for (var i=0;i<this.machines.length;i++) this.machines[i].draggable(true)
            for (var i=0;i<this.queues.length;i++) {
                console.log(toRaw(this.queues[i]))
                this.queues[i].draggable(true)
            }
            this.link = false
            this.isDrawing = false
        },
        Link(e){
            this.noArrows()
            this.$refs.link.style.background="yellow"
            this.$refs.mouse.style.background="#12cc7c"
            for (var i=0;i<this.machines.length;i++) this.machines[i].draggable(false)
            for (var i=0;i<this.queues.length;i++) this.queues[i].draggable(false)
            this.link = true
        },
        async StartArrow(e){
            this.isDrawing = true
            this.arrow = new Konva.Arrow({
                points: [e.target.parent.x(),e.target.parent.y()],
                pointerLength: 10,
                pointerWidth: 10,
                fill: 'black',
                stroke: 'black',
                strokeWidth: 1,
            })
            if(e.target.parent.children[0].name()=='queue'){
                this.arrow.points([e.target.parent.x()+25,e.target.parent.y()+25])
            }
            this.arrowSource = [this.arrow.points()[0],this.arrow.points()[1],e.target.parent.children[0].name(),e.target]
            this.layer.add(toRaw(this.arrow)).batchDraw()
        },
        async ContinueArrow(e){
            let coor= this.position(e,this.arrowSource[2]);
            let newX=coor[0],newY=coor[1]
            let centerX = this.arrowSource[0]
            let centerY = this.arrowSource[1]
            this.arrow.points()[0] = centerX+newX
            this.arrow.points()[1] = centerY+newY
            this.arrow.points()[2] = this.stage.getPointerPosition().x-10
            this.arrow.points()[3] = this.stage.getPointerPosition().y-10
            this.layer.batchDraw()
        },
        async EndArrow(e){
            let coor= this.position(e,e.target.parent.children[0].name(),true)
            let newX=coor[0],newY=coor[1]
            this.arrow.points()[2] = e.target.parent.getX()-newX
            this.arrow.points()[3] = e.target.parent.getY()-newY
            this.arrowsList.push(this.arrow)
            this.layer.batchDraw()
            this.isDrawing = false
            let machine = this.arrowSource[2]=='machine'? true : false
            let srcId = toRaw(this.arrowSource[3]).parent.id()
            let desId = toRaw(e.target).parent.id()
            const response = axios.post(this.url+'connect?srcID='+srcId+'&destID='+desId+'&isSrcMachine='+machine)
        },
        position(e,type,end=false){
            let pointedX = this.stage.getPointerPosition().x , pointedY = this.stage.getPointerPosition().y
            let centerX = this.arrowSource[0] , centerY = this.arrowSource[1]
            let newX=0,newY=0
            let angle = Math.atan((centerY-pointedY)/(pointedX-centerX))
            if (type == 'machine'){
                let distance = Math.sqrt(Math.pow((pointedX-centerX),2)+Math.pow((pointedY-centerY),2))
                newX = 30/distance*(pointedX-centerX)
                newY = 30/distance*(pointedY-centerY)
            }
            if (type =='queue'){
                if (Math.abs(angle*180/Math.PI)>45){
                    newY = 25 
                    newX = 25/Math.tan(angle)
                    if (centerY>pointedY) newY *= -1
                    if (centerY<pointedY) newX *= -1
                }
                else{
                    newX = 25
                    newY = 25*Math.tan(angle)
                    if (centerX>pointedX) newX *= -1
                    if (centerX<pointedX) newY *= -1
                }
                if (end){
                    newX-=25
                    newY-=25
                }
            }
            return [newX,newY]
        },
        Clear(){
            this.noArrows()
            this.isDrawing = false,
            this.arrow = null,
            this.arrowSource = [],
            this.arrowsList = [],
            this.machines=[],
            this.queues = [],
            this.Qindex = 0,
            this.Mindex = 0,
            this.Aindex = 0,
            this.isPosted= [true],
            this.isposted= true,
            this.link = false
            this.finished = true
            this.layer.destroyChildren().batchDraw()
            const response = axios.post(this.url+'clear')
        },
        async run(type){
            this.$refs.link.style.background="#12cc7c"
            this.$refs.mouse.style.background="#12cc7c"
            if (type=='run'){
                let productsNumber = this.$refs.products.value
                if (productsNumber<1) alert("Number of Products Must be between 1 and 100")
                else if (productsNumber>100) ("Number of Products Must be between 1 and 100")
                else {
                    // this.$refs.run.style.background="yellow"
                    const request = await axios.post(this.url+'addProducts?number='+productsNumber)
                    const response = await axios.post(this.url+'run')
                    this.finished = false
                    setInterval(() => {
                        if (!this.finished) {
                            this.Perform()
                            // this.$refs.run.style.background="yellow"
                        }
                        // else this.$refs.run.style.background="#12cc7c"
                    },500)
                }
                // this.$refs.run.style.background="#12cc7c"
            }
            else{
                // this.$refs.replay.style.background="yellow"
                const response = await axios.get(this.url+'replay')
                if (response.data){
                    this.finished = false
                    setInterval(() => {
                        if (!this.finished) {
                            this.Perform()
                            // this.$refs.replay.style.background="yellow"
                        }
                        // else this.$refs.replay.style.background="#12cc7c"
                    },500)
                }
                // this.$refs.replay.style.background="#12cc7c"
            }
        },
        async Perform(){
            const response = await axios.get(this.url+'update')
            this.applyStates(response.data)
        },
        applyStates(data){
            if (data.isFinished) this.finished=true
            for (var i=0;i<data.colors.length;i++){
                let id = data.colors[i].id
                let color = data.colors[i].color
                this.machines[id-1].children[0].fill(color)
            }
            for (var i=0;i<data.products.length;i++){
                let id = data.products[i].id
                let product = data.products[i].products
                console.log(this.queues[id-1].children[1].text())
                let len = this.queues[id-1].children[1].text().length
                let textNode = this.queues[id-1].children[1];
                textNode.text(textNode.text().slice(0,len-1).concat(product));
            }
        }
    },

    mounted() {
        this.stage = new Konva.Stage({
            container: this.$refs.container,
            width: window.innerWidth,
            height: window.innerHeight,
            id :0,
            name: 'container'
        });
        this.layer = new Konva.Layer();
        this.stage.add(this.layer);
        this.stage.on('mousedown',(e)=>{
            console.log(e.target)
            if (this.link && (toRaw(this.machines).includes(e.target.parent)||toRaw(this.queues).includes(e.target.parent) )){
                if (!this.isDrawing) this.StartArrow(e)
                else {
                    if (e.target.parent.name() == this.arrowSource[2]) {
                        alert("You Must Link Machines with Queues")
                    }
                    else this.EndArrow(e)
                }
            }
        })
        this.stage.on('mousemove',(e)=>{
            if (this.link && this.isDrawing){
                this.ContinueArrow(e.evt)
            }
        })
    }, 
    
}


</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
    #container{
        background-color: white;
        filter: blur(0px);
        box-shadow: 0.5px 0.5px 5px 3px rgb(135, 135, 135) ;
        width: 98%;
        height: 80%;
        margin: 0px 1%;
    }
    #bar{
        width: 98%;
        margin: 0px 1%;
        margin-bottom: 7px;
        margin-top: 5px;
        padding: 3px 0px;
    }
    .button{
        margin: 3px;
        background-color: #12cc7c;
        border: 0.5px rgb(99, 99, 99) solid;
        /* width: 7%; */
        width: 23%;
        height: 45px;
        font-family: monospace;
        font-weight: bold;
        font-size: 16px;
        border-radius: 5px;
    }
    .button:hover{
        background-color:#80e6ba;
    }
    .tools{
        display:inline-flex;
        width: 33.3333%;
    }
    input{
        /* padding: 15px 0px 0px 0px; */
        border-radius: 4px;
        /* height: 25px; */
    }
</style>