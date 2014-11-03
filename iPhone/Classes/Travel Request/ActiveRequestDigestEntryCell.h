//
//  ActiveRequestDigestEntryCell.h
//  ConcurMobile
//
//  Created by laurent mery on 26/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
@class CTETravelRequestEntry;

@interface ActiveRequestDigestEntryCell : UITableViewCell

-(void)updateCellWithEntry:(CTETravelRequestEntry*)entry;
-(void)updateCellWithTotal:(NSString*)total endCurrencyCode:(NSString*)currencyCode;

@end
