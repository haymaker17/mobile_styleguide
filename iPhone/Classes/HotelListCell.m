//
//  HotelListCell.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/23/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelListCell.h"
#import "HotelCollectionViewController.h"


@implementation HotelListCell

@synthesize parentMVC;
@synthesize hotelIndex;
@synthesize logoView;
@synthesize name;
@synthesize address1;
@synthesize address2;
@synthesize address3;
@synthesize phone;
@synthesize distance;
@synthesize amount;
@synthesize starRating;
@synthesize shadowStarRating;
@synthesize notRated, lblStarting, ivStars, ivDiamonds;

-(IBAction)btnAddress:(id)sender
{
	[parentMVC addressPressed:self];
}

-(IBAction)btnPhone:(id)sender
{
	[parentMVC phonePressed:self];
}


@end
