package com.example.gmapsample.model

class ProfileImage {
    public var imageId = 0
    public lateinit var imageName: String

    constructor(imageId: Int, imageName: String) {
        this.imageId = imageId
        this.imageName = imageName
    }
}