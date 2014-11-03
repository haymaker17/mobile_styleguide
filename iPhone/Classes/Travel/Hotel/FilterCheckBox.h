//
//  FilterCheckBox.h
//  ConcurMobile
//
//  Created by Ray Chi on 9/18/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface FilterCheckBox : UIControl <UIAppearance>

@property (nonatomic,assign,getter = isOn) BOOL on;
@property (nonatomic) BOOL isClick;

@property (nonatomic, strong) NSString *text;
@property (nonatomic) NSInteger value;

/**
 *  Type of the check box:
 1-- "All" with 17 font size
 2-- "3+" with 3 stars image
 3-- "5 miles" with 15 font size
 4-- future work.......
 */
@property (nonatomic) NSInteger type;

- (void)setOn:(BOOL)on;

- (void) changeImageStars:(NSInteger)starNo;


@end

@interface UIImage (FilterCheckBox)

- (UIImage *)imageTintedWithColor:(UIColor *)color;

@end
