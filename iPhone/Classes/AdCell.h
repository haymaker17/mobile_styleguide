//
//  AdCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 1/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "RootViewController.h"

@interface AdCell : UITableViewCell 
{
	RootViewController *rootVC;
	NSUInteger *currentRow;
}

@property (nonatomic, retain) RootViewController *rootVC;
@property (nonatomic) NSUInteger *currentRow;

@end
