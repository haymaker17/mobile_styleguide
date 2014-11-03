//
//  AbstractTableViewCellData.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

/*
 * Abstract class - Do not use this class as-is or instantiate it
 * =========== This class must be inherited =======
 * TableviewCell Data.
 * TableCellData class is used to hold the cell data. 
 *
 */


#import <Foundation/Foundation.h>

@interface AbstractTableViewCellData : NSObject

typedef NS_ENUM(NSUInteger, CellType) {
    kTextBox,
    kPickerView,
    kDatePicker,
    kList,
    kLocationList,
    kBool,
    kReadOnly
};

@property CGFloat cellHeight;
@property CellType cellType;
@property (strong, nonatomic) NSString *cellIdentifier;
@property (strong, nonatomic) NSDictionary *keyValue;
// non-localized cell name for look up
@property (strong, nonatomic) NSString *cellName;
@end
