//
//  CityCell2.h
//  ConcurMobile
//
//  Created by Ray Chi on 12/3/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CityCell2 : UITableViewCell <UITextFieldDelegate>

@property (strong, nonatomic) IBOutlet UITextField *textField1;
@property (strong, nonatomic) IBOutlet UITextField *textField2;
@property (strong, nonatomic) IBOutlet UILabel *underLine;

@end
