//
//  AirLayoverCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/16/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AirLayoverCell : UITableViewCell
{
    UILabel     *lblLayover;
}

@property (strong, nonatomic) IBOutlet UILabel *lblLayover;

@end
