//
//  PhotoAlbumTableViewCellData.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 26/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "PhotoAlbumTableViewCellData.h"

@interface PhotoAlbumTableViewCellData()
@property (nonatomic, strong) CTEHotelCellData *hotelData;
@end

@implementation PhotoAlbumTableViewCellData

-(instancetype)initWithCTEHotelCellData:(CTEHotelCellData *)cteHotelCellData
{
    self = [super init];
    if (!self) {
        return nil;
    }
    self.hotelData = cteHotelCellData;
    self.cellIdentifier = @"PhotoAlbumTableViewCell";
    self.cellHeight = 108.0 ;
    
    return self;
}

-(instancetype)init
{
    self = [self initWithCTEHotelCellData:nil];
    return self;
}

-(CTEHotelCellData *)getHotelData
{
    return self.hotelData;
}

@end
