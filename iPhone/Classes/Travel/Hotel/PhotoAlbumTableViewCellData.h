//
//  PhotoAlbumTableViewCellData.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 26/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "AbstractTableViewCellData.h"
#import "CTEHotelCellData.h"

@interface PhotoAlbumTableViewCellData : AbstractTableViewCellData

@property (nonatomic) BOOL isCellHeightSetAccordingToContentSize;

-(instancetype)initWithCTEHotelCellData:(CTEHotelCellData *)cteHotelCellData;
-(CTEHotelCellData *)getHotelData;

@end
