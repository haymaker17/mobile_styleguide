//
//  HotelSearchResultListDataSource.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 6/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelSearchResultListDataSource.h"
#import "CTEHotelSearch.h"

@interface HotelSearchResultListDataSource()

@property (strong,nonatomic) NSMutableArray *hotelSearchResultlist ;

@end


@implementation HotelSearchResultListDataSource

-(instancetype)init
{
    self = [super init];
    
    if (!self)
        return nil;
    self.hotelSearchResultlist = [[NSMutableArray alloc] init];
    return self;
}


-(void)loadContent
{
    //TODO :  Make the network request here .
    
 
}


@end
